package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EmitirTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEmitidoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.EmisionTurnoService;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.TurnoEstadoService;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.TurnoService;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;
    private final EmisionTurnoService emisionTurnoService;
    private final TurnoEstadoService turnoEstadoService;

    public TurnoController(TurnoService turnoService, EmisionTurnoService emisionTurnoService,
            TurnoEstadoService turnoEstadoService) {
        this.turnoService = turnoService;
        this.emisionTurnoService = emisionTurnoService;
        this.turnoEstadoService = turnoEstadoService;
    }

    @GetMapping("/{codigo}")
    public TurnoPublicoResponse consultarTurno(@PathVariable String codigo) {
        return turnoService.consultarPorCodigo(codigo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurnoEmitidoResponse emitirTurno(@RequestBody EmitirTurnoRequest request) {
        return emisionTurnoService.emitir(request);
    }

    @PatchMapping("/{id}/estado")
    public TurnoEstadoResponse cambiarEstado(@PathVariable Long id,
            @RequestBody CambiarEstadoTurnoRequest request) {
        return turnoEstadoService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarTurno(@PathVariable Long id) {
        turnoEstadoService.cancelar(id);
    }
}
