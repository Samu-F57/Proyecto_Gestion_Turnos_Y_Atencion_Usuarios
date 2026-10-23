package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.TipoDocumento;

public record SolicitanteRequest(
    TipoDocumento tipoDocumento,
    String numeroDocumento,
    String nombreCompleto,
    String correo,
    String telefono
) {
}
