package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ModuloEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.RolUsuario;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.LlamarSiguienteTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoLlamadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ModuloRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@Service
public class LlamadoTurnoService {

    private static final List<EstadoTurno> ATENCION_ACTIVA =
            List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION);

    private final ModuloRepository moduloRepository;
    private final UsuarioInternoRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final HistorialTurnoRepository historialRepository;
    private final Clock clock;

    public LlamadoTurnoService(ModuloRepository moduloRepository,
            UsuarioInternoRepository usuarioRepository, TurnoRepository turnoRepository,
            HistorialTurnoRepository historialRepository, Clock clock) {
        this.moduloRepository = moduloRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.historialRepository = historialRepository;
        this.clock = clock;
    }

    @Transactional
    public TurnoLlamadoResponse llamarSiguiente(Long moduloId, LlamarSiguienteTurnoRequest request) {
        if (request == null || request.agenteId() == null) {
            throw new SolicitudInvalidaException("agenteId es obligatorio");
        }
        ModuloEntity modulo = moduloRepository.findById(moduloId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el módulo " + moduloId));
        UsuarioInternoEntity agente = usuarioRepository.findById(request.agenteId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el usuario " + request.agenteId()));
        validarModuloYAgente(modulo, agente);

        // RN-07: un módulo no puede tener dos atenciones activas
        if (turnoRepository.existsByModuloIdAndEstadoIn(modulo.getId(), ATENCION_ACTIVA)) {
            throw new ReglaNegocioException("El módulo ya tiene un turno en curso");
        }

        // RN-06: solo los servicios habilitados para el módulo
        List<Long> servicioIds = modulo.getServicios().stream().map(ServicioEntity::getId).toList();
        if (servicioIds.isEmpty()) {
            throw new ReglaNegocioException("El módulo no tiene servicios habilitados");
        }

        LocalDateTime ahora = LocalDateTime.now(clock);
        LocalDate fechaOperativa = ahora.toLocalDate();
        // RN-08: prioridad y, dentro de ella, orden de emisión
        TurnoEntity elegido = turnoRepository.buscarElegibles(
                        servicioIds, fechaOperativa, EstadoTurno.EN_ESPERA, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay turnos en espera para el módulo " + modulo.getNombre()));

        // Bloquea la fila y revisa que nadie la haya llamado mientras tanto
        TurnoEntity turno = turnoRepository.bloquearPorId(elegido.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno " + elegido.getId()));
        if (turno.getEstado() != EstadoTurno.EN_ESPERA) {
            throw new ReglaNegocioException("El turno ya fue llamado por otro módulo");
        }

        // RN-09: registra módulo, agente, fecha y entrada de historial
        turno.setEstado(EstadoTurno.LLAMADO);
        turno.setModulo(modulo);
        turno.setAgente(agente);
        turnoRepository.save(turno);

        HistorialTurnoEntity evento = new HistorialTurnoEntity();
        evento.setTurno(turno);
        evento.setEstadoAnterior(EstadoTurno.EN_ESPERA);
        evento.setEstadoNuevo(EstadoTurno.LLAMADO);
        evento.setFechaEvento(ahora);
        evento.setModulo(modulo);
        evento.setUsuario(agente);
        historialRepository.save(evento);

        return new TurnoLlamadoResponse(
                turno.getCodigo(),
                turno.getEstado(),
                modulo.getNombre(),
                agente.getNombreCompleto(),
                ahora
        );
    }

    private void validarModuloYAgente(ModuloEntity modulo, UsuarioInternoEntity agente) {
        if (modulo.getEstado() != EstadoModulo.ABIERTO) {
            throw new ReglaNegocioException("El módulo " + modulo.getNombre() + " no está abierto");
        }
        if (agente.getRol() != RolUsuario.AGENTE || !agente.isActivo()) {
            throw new ReglaNegocioException("El usuario " + agente.getId() + " no es un agente activo");
        }
        if (modulo.getAgente() == null || !modulo.getAgente().getId().equals(agente.getId())) {
            throw new ReglaNegocioException("El módulo está asignado a otro agente");
        }
    }
}
