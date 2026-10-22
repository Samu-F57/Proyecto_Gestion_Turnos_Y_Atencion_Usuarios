package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;

    public TurnoService(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Transactional(readOnly = true)
    public TurnoPublicoResponse consultarPorCodigo(String codigo) {
        TurnoEntity turno = turnoRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el turno " + codigo));
        return new TurnoPublicoResponse(
                turno.getCodigo(),
                turno.getServicio().getNombre(),
                turno.getEstado(),
                moduloVisible(turno),
                turno.getFechaEmision()
        );
    }

    // El módulo solo se muestra mientras el turno está llamado o en atención
    private String moduloVisible(TurnoEntity turno) {
        boolean activo = turno.getEstado() == EstadoTurno.LLAMADO
                || turno.getEstado() == EstadoTurno.EN_ATENCION;
        return activo && turno.getModulo() != null ? turno.getModulo().getNombre() : null;
    }
}
