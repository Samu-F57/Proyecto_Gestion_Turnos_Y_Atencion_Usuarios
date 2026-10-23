package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record TurnoEmitidoResponse(
    Long id,
    String codigo,
    String servicio,
    EstadoTurno estado,
    LocalDate fechaOperativa,
    int consecutivo,
    LocalDateTime fechaEmision
) {
}
