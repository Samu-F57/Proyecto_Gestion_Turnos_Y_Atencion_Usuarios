package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalTime;

public record ServicioResponse(
    Long id,
    String codigo,
    String nombre,
    String descripcion,
    LocalTime horaApertura,
    LocalTime horaCierre
) {
}
