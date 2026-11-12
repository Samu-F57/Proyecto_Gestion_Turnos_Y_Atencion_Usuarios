package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ModuloEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.RolUsuario;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoModuloRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ModuloEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ModuloRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@Service
public class ModuloService {

    private static final List<EstadoTurno> ATENCION_ACTIVA =
            List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION);

    private final ModuloRepository moduloRepository;
    private final UsuarioInternoRepository usuarioRepository;
    private final TurnoRepository turnoRepository;

    public ModuloService(ModuloRepository moduloRepository,
            UsuarioInternoRepository usuarioRepository, TurnoRepository turnoRepository) {
        this.moduloRepository = moduloRepository;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
    }

    @Transactional
    public ModuloEstadoResponse cambiarEstado(Long id, CambiarEstadoModuloRequest request) {
        if (request == null || request.estado() == null) {
            throw new SolicitudInvalidaException("estado es obligatorio");
        }
        ModuloEntity modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el módulo " + id));
        EstadoModulo destino = request.estado();
        if (modulo.getEstado() == destino) {
            throw new ReglaNegocioException("El módulo ya está en estado " + destino);
        }

        if (destino == EstadoModulo.ABIERTO) {
            abrir(modulo, request.agenteId());
        } else if (destino == EstadoModulo.PAUSADO) {
            pausar(modulo);
        } else {
            cerrar(modulo);
        }

        ModuloEntity guardado = moduloRepository.save(modulo);
        return new ModuloEstadoResponse(
                guardado.getId(),
                guardado.getCodigo(),
                guardado.getEstado(),
                guardado.getAgente() == null ? null : guardado.getAgente().getNombreCompleto()
        );
    }

    // Al abrir se necesita un agente; si el módulo estaba pausado conserva el suyo
    private void abrir(ModuloEntity modulo, Long agenteId) {
        UsuarioInternoEntity agente = modulo.getAgente();
        if (agenteId != null) {
            agente = usuarioRepository.findById(agenteId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + agenteId));
            if (agente.getRol() != RolUsuario.AGENTE || !agente.isActivo()) {
                throw new ReglaNegocioException("El usuario " + agenteId + " no es un agente activo");
            }
        } else if (agente == null) {
            throw new SolicitudInvalidaException("agenteId es obligatorio para abrir el módulo");
        }
        modulo.setAgente(agente);
        modulo.setEstado(EstadoModulo.ABIERTO);
    }

    private void pausar(ModuloEntity modulo) {
        if (modulo.getEstado() != EstadoModulo.ABIERTO) {
            throw new ReglaNegocioException("Solo un módulo abierto puede pausarse");
        }
        modulo.setEstado(EstadoModulo.PAUSADO);
    }

    // RN-07: no se cierra un módulo con un turno llamado o en atención
    private void cerrar(ModuloEntity modulo) {
        if (turnoRepository.existsByModuloIdAndEstadoIn(modulo.getId(), ATENCION_ACTIVA)) {
            throw new ReglaNegocioException("El módulo tiene un turno en curso");
        }
        modulo.setEstado(EstadoModulo.CERRADO);
        modulo.setAgente(null);
    }
}
