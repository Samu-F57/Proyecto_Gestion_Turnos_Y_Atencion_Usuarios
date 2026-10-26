package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDateTime;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record TurnoPublicoResponse(
    String codigo,
    String servicio,
    EstadoTurno estado,
    String modulo,
    LocalDateTime fechaEmision
) {
}
