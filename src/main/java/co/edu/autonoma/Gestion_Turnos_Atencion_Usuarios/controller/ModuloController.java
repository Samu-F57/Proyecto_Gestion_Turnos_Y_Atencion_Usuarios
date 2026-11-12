package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoModuloRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.LlamarSiguienteTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ModuloEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoLlamadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.LlamadoTurnoService;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.ModuloService;

@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    private final ModuloService moduloService;
    private final LlamadoTurnoService llamadoTurnoService;

    public ModuloController(ModuloService moduloService, LlamadoTurnoService llamadoTurnoService) {
        this.moduloService = moduloService;
        this.llamadoTurnoService = llamadoTurnoService;
    }

    @PatchMapping("/{id}/estado")
    public ModuloEstadoResponse cambiarEstado(@PathVariable Long id,
            @RequestBody CambiarEstadoModuloRequest request) {
        return moduloService.cambiarEstado(id, request);
    }

    @PostMapping("/{id}/siguiente-turno")
    public TurnoLlamadoResponse llamarSiguienteTurno(@PathVariable Long id,
            @RequestBody LlamarSiguienteTurnoRequest request) {
        return llamadoTurnoService.llamarSiguiente(id, request);
    }
}
