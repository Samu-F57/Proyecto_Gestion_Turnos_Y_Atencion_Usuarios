package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ActualizarServicioRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
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

    // Reemplaza la configuración completa del servicio
    @Transactional
    public ServicioResponse reemplazar(Long id, ActualizarServicioRequest request) {
        validar(request);
        ServicioEntity servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el servicio " + id));
        if (servicioRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new ReglaNegocioException("Ya existe un servicio con el código " + request.codigo());
        }
        if (servicioRepository.existsByNombreAndIdNot(request.nombre(), id)) {
            throw new ReglaNegocioException("Ya existe un servicio con el nombre " + request.nombre());
        }

        servicio.setCodigo(request.codigo());
        servicio.setNombre(request.nombre());
        servicio.setDescripcion(request.descripcion());
        servicio.setActivo(request.activo());
        servicio.setHoraApertura(request.horaApertura());
        servicio.setHoraCierre(request.horaCierre());
        return aRespuesta(servicioRepository.save(servicio));
    }

    private void validar(ActualizarServicioRequest request) {
        if (request == null || request.codigo() == null || !request.codigo().matches("[A-Z]{2,10}")) {
            throw new SolicitudInvalidaException("codigo es obligatorio: de 2 a 10 letras mayúsculas");
        }
        if (request.nombre() == null || request.nombre().isBlank() || request.nombre().length() > 100) {
            throw new SolicitudInvalidaException("nombre es obligatorio y admite hasta 100 caracteres");
        }
        if (request.descripcion() != null && request.descripcion().length() > 255) {
            throw new SolicitudInvalidaException("descripcion admite hasta 255 caracteres");
        }
        if (request.horaApertura() == null || request.horaCierre() == null
                || !request.horaCierre().isAfter(request.horaApertura())) {
            throw new SolicitudInvalidaException("horaCierre debe ser posterior a horaApertura");
        }
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
