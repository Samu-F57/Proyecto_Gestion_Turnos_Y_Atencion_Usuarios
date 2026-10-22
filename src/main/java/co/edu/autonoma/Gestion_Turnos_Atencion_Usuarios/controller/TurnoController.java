package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.TurnoService;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping("/{codigo}")
    public TurnoPublicoResponse consultarTurno(@PathVariable String codigo) {
        return turnoService.consultarPorCodigo(codigo);
    }
}
