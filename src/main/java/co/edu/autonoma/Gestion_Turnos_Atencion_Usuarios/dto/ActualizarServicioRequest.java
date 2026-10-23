package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import java.time.LocalTime;

public record ActualizarServicioRequest(
    String codigo,
    String nombre,
    String descripcion,
    boolean activo,
    LocalTime horaApertura,
    LocalTime horaCierre
) {
}
