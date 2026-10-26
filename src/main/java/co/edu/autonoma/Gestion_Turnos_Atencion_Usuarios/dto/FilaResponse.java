package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDate;
import java.util.List;

public record FilaResponse(
    Long servicioId,
    String servicio,
    LocalDate fechaOperativa,
    int totalEnEspera,
    List<FilaTurnoResponse> turnos
) {
}
