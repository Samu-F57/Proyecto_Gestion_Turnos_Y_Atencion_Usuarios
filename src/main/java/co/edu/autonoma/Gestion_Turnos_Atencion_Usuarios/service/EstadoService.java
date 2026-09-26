package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import org.springframework.stereotype.Service;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EstadoResponse;

@Service
public class EstadoService {

    public EstadoResponse consultarEstado() {
        return new EstadoResponse(
                "reservas-api",
                "disponible"
        );
    }
}