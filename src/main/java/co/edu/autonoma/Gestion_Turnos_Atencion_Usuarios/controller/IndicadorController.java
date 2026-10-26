package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.IndicadoresResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.IndicadorService;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final IndicadorService indicadorService;

    public IndicadorController(IndicadorService indicadorService) {
        this.indicadorService = indicadorService;
    }

    @GetMapping
    public IndicadoresResponse consultarIndicadores(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return indicadorService.calcular(fecha);
    }
}
