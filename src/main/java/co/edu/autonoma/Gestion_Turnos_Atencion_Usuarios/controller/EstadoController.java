package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.EstadoService;

@RestController
@RequestMapping("/api/estado")
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

    @GetMapping
    public EstadoResponse consultarEstado() {
        return estadoService.consultarEstado();
    }
}