package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaTurnoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.FilaService;

@WebMvcTest(FilaController.class)
class FilaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilaService filaService;

    @Test
    void consultarFilaResponde200ConOrdenYTiempos() throws Exception {
        when(filaService.consultarFila(1L)).thenReturn(new FilaResponse(
                1L, "Orientación", LocalDate.of(2026, 10, 5), 1,
                List.of(new FilaTurnoResponse(1, "ORI-261005-002", "PREFERENCIAL",
                        LocalDateTime.of(2026, 10, 5, 8, 10), 1200))));

        mockMvc.perform(get("/api/filas").param("servicioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicioId").value(1))
                .andExpect(jsonPath("$.totalEnEspera").value(1))
                .andExpect(jsonPath("$.turnos[0].orden").value(1))
                .andExpect(jsonPath("$.turnos[0].codigo").value("ORI-261005-002"))
                .andExpect(jsonPath("$.turnos[0].prioridad").value("PREFERENCIAL"))
                .andExpect(jsonPath("$.turnos[0].tiempoEsperaSegundos").value(1200));
    }

    @Test
    void consultarFilaSinServicioIdResponde400() throws Exception {
        mockMvc.perform(get("/api/filas"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFilaConServicioIdNoNumericoResponde400() throws Exception {
        mockMvc.perform(get("/api/filas").param("servicioId", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFilaDeServicioInexistenteResponde404() throws Exception {
        when(filaService.consultarFila(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe el servicio 99"));

        mockMvc.perform(get("/api/filas").param("servicioId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el servicio 99"));
    }
}
