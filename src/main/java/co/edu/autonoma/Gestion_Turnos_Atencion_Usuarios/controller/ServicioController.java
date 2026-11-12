package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ActualizarServicioRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.ServicioService;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<ServicioResponse> listarServiciosActivos() {
        return servicioService.listarActivos();
    }

    @PutMapping("/{id}")
    public ServicioResponse reemplazarServicio(@PathVariable Long id,
            @RequestBody ActualizarServicioRequest request) {
        return servicioService.reemplazar(id, request);
    }
}
