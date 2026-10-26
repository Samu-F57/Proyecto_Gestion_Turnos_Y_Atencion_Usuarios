package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalDate;

public record IndicadoresResponse(
    LocalDate fecha,
    long cantidadAtendida,
    Double tiempoPromedioEsperaSegundos,
    Double tiempoPromedioAtencionSegundos
) {
}
