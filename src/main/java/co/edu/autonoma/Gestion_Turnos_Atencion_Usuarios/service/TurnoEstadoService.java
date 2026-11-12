package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@Service
public class TurnoEstadoService {

    private static final int LIMITE_OBSERVACION = 255;

    // RN-05, RN-10 y RN-11. LLAMADO solo se alcanza al llamar el siguiente turno
    private static final Map<EstadoTurno, Set<EstadoTurno>> TRANSICIONES = Map.of(
            EstadoTurno.EN_ESPERA, EnumSet.of(EstadoTurno.CANCELADO),
            EstadoTurno.LLAMADO, EnumSet.of(EstadoTurno.EN_ATENCION, EstadoTurno.NO_PRESENTE,
                    EstadoTurno.CANCELADO),
            EstadoTurno.EN_ATENCION, EnumSet.of(EstadoTurno.FINALIZADO));

    private final TurnoRepository turnoRepository;
    private final UsuarioInternoRepository usuarioRepository;
    private final HistorialTurnoRepository historialRepository;
    private final Clock clock;

    public TurnoEstadoService(TurnoRepository turnoRepository,
            UsuarioInternoRepository usuarioRepository,
            HistorialTurnoRepository historialRepository, Clock clock) {
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialRepository = historialRepository;
        this.clock = clock;
    }

    @Transactional
    public TurnoEstadoResponse cambiarEstado(Long id, CambiarEstadoTurnoRequest request) {
        if (request == null || request.estado() == null) {
            throw new SolicitudInvalidaException("estado es obligatorio");
        }
        if (request.observacion() != null && request.observacion().length() > LIMITE_OBSERVACION) {
            throw new SolicitudInvalidaException(
                    "observacion no puede superar " + LIMITE_OBSERVACION + " caracteres");
        }
        TurnoEntity turno = buscarTurno(id);
        UsuarioInternoEntity usuario = request.usuarioId() == null ? null
                : usuarioRepository.findById(request.usuarioId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No existe el usuario " + request.usuarioId()));

        EstadoTurno destino = request.estado();
        if (!TRANSICIONES.getOrDefault(turno.getEstado(), Set.of()).contains(destino)) {
            throw new ReglaNegocioException(
                    "Transición no permitida: " + turno.getEstado() + " a " + destino);
        }
        transitar(turno, destino, usuario, request.observacion());
        return new TurnoEstadoResponse(turno.getId(), turno.getCodigo(), turno.getEstado());
    }

    // RN-11: solo se cancela antes de iniciar la atención. El turno no se borra
    @Transactional
    public void cancelar(Long id) {
        TurnoEntity turno = buscarTurno(id);
        if (turno.getEstado() != EstadoTurno.EN_ESPERA && turno.getEstado() != EstadoTurno.LLAMADO) {
            throw new ReglaNegocioException("Solo se puede cancelar un turno antes de iniciar su atención");
        }
        transitar(turno, EstadoTurno.CANCELADO, null, "Turno cancelado");
    }

    private TurnoEntity buscarTurno(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el turno " + id));
    }

    private void transitar(TurnoEntity turno, EstadoTurno destino,
            UsuarioInternoEntity usuario, String observacion) {
        EstadoTurno anterior = turno.getEstado();
        turno.setEstado(destino);
        turnoRepository.save(turno);

        HistorialTurnoEntity evento = new HistorialTurnoEntity();
        evento.setTurno(turno);
        evento.setEstadoAnterior(anterior);
        evento.setEstadoNuevo(destino);
        evento.setFechaEvento(LocalDateTime.now(clock));
        evento.setModulo(turno.getModulo());
        evento.setUsuario(usuario);
        evento.setObservacion(observacion);
        historialRepository.save(evento);
    }
}
