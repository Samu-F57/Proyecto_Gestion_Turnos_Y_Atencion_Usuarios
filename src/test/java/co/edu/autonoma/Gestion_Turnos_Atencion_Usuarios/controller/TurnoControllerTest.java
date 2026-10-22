package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.TurnoService;

@WebMvcTest(TurnoController.class)
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;

    @Test
    void consultarTurnoExistenteResponde200() throws Exception {
        when(turnoService.consultarPorCodigo("ORI-261005-007")).thenReturn(new TurnoPublicoResponse(
                "ORI-261005-007", "Orientación", EstadoTurno.LLAMADO, "Módulo 1",
                LocalDateTime.of(2026, 10, 5, 8, 0)));

        mockMvc.perform(get("/api/turnos/ORI-261005-007"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ORI-261005-007"))
                .andExpect(jsonPath("$.servicio").value("Orientación"))
                .andExpect(jsonPath("$.estado").value("LLAMADO"))
                .andExpect(jsonPath("$.modulo").value("Módulo 1"))
                .andExpect(jsonPath("$.fechaEmision").isString());
    }

    @Test
    void consultarTurnoInexistenteResponde404ConMensaje() throws Exception {
        when(turnoService.consultarPorCodigo("XXX"))
                .thenThrow(new RecursoNoEncontradoException("No existe el turno XXX"));

        mockMvc.perform(get("/api/turnos/XXX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el turno XXX"));
    }
}
