package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;

public record ModuloEstadoResponse(
    Long id,
    String codigo,
    EstadoModulo estado,
    String agente
) {
}
