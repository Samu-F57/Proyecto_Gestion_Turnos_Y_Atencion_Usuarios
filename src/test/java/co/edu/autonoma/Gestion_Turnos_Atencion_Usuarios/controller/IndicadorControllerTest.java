package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.IndicadoresResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.IndicadorService;

@WebMvcTest(IndicadorController.class)
class IndicadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IndicadorService indicadorService;

    @Test
    void consultarIndicadoresResponde200() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 10, 5);
        when(indicadorService.calcular(fecha))
                .thenReturn(new IndicadoresResponse(fecha, 1, 750.0, 1080.0));

        mockMvc.perform(get("/api/indicadores").param("fecha", "2026-10-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.cantidadAtendida").value(1))
                .andExpect(jsonPath("$.tiempoPromedioEsperaSegundos").value(750.0))
                .andExpect(jsonPath("$.tiempoPromedioAtencionSegundos").value(1080.0));
    }

    @Test
    void consultarIndicadoresSinFechaResponde400() throws Exception {
        mockMvc.perform(get("/api/indicadores"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarIndicadoresConFechaInvalidaResponde400() throws Exception {
        mockMvc.perform(get("/api/indicadores").param("fecha", "05/10/2026"))
                .andExpect(status().isBadRequest());
    }
}
