package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.ServicioService;

@WebMvcTest(ServicioController.class)
class ServicioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServicioService servicioService;

    @Test
    void listarServiciosActivosResponde200ConLaLista() throws Exception {
        when(servicioService.listarActivos()).thenReturn(List.of(new ServicioResponse(
                1L, "ORI", "Orientación", "Información general",
                LocalTime.of(8, 0), LocalTime.of(17, 0))));

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigo").value("ORI"))
                .andExpect(jsonPath("$[0].nombre").value("Orientación"))
                .andExpect(jsonPath("$[0].horaApertura").isString())
                .andExpect(jsonPath("$[0].horaCierre").isString());
    }

    @Test
    void listarServiciosSinResultadosResponde200ConListaVacia() throws Exception {
        when(servicioService.listarActivos()).thenReturn(List.of());

        mockMvc.perform(get("/api/servicios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
