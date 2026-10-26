package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarActivos() {
        return servicioRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    private ServicioResponse aRespuesta(ServicioEntity servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getCodigo(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getHoraApertura(),
                servicio.getHoraCierre()
        );
    }
}
