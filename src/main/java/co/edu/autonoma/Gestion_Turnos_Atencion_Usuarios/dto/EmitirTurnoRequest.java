package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

public record EmitirTurnoRequest(
    Long servicioId,
    SolicitanteRequest solicitante,
    Long prioridadId
) {
}
