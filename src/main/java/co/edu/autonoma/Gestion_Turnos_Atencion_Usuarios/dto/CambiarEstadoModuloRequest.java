package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;

public record CambiarEstadoModuloRequest(
    EstadoModulo estado,
    Long agenteId
) {
}
