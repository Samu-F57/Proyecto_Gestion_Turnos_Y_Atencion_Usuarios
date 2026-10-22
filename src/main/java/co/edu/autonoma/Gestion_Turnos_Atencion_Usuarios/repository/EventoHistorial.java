package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.time.LocalDateTime;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public record EventoHistorial(
    Long turnoId,
    EstadoTurno estado,
    LocalDateTime fecha
) {
}
