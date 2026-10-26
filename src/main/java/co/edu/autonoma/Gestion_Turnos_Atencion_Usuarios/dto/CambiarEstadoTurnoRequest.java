package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record CambiarEstadoTurnoRequest(
    EstadoTurno estado,
    Long usuarioId,
    String observacion
) {
}
