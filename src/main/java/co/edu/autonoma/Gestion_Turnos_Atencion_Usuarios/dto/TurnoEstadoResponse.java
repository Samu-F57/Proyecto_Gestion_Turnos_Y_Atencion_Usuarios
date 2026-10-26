package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record TurnoEstadoResponse(
    Long id,
    String codigo,
    EstadoTurno estado
) {
}
