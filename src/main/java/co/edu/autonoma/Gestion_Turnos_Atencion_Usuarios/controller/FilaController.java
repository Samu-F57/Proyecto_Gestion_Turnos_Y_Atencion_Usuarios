package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.FilaService;

@RestController
@RequestMapping("/api/filas")
public class FilaController {

    private final FilaService filaService;

    public FilaController(FilaService filaService) {
        this.filaService = filaService;
    }

    @GetMapping
    public FilaResponse consultarFila(@RequestParam Long servicioId) {
        return filaService.consultarFila(servicioId);
    }
}
