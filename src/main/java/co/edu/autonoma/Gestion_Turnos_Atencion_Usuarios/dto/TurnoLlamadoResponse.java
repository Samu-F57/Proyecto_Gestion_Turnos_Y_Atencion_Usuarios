package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDateTime;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record TurnoLlamadoResponse(
    String codigo,
    EstadoTurno estado,
    String modulo,
    String agente,
    LocalDateTime fechaLlamado
) {
}
