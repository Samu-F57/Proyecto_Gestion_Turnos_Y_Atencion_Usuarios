package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDateTime;

public record FilaTurnoResponse(
    int orden,
    String codigo,
    String prioridad,
    LocalDateTime fechaEmision,
    long tiempoEsperaSegundos
) {
}
