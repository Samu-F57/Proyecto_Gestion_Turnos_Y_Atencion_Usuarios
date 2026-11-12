package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ActualizarServicioRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
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

    private static final String CUERPO_PUT = "{\"codigo\": \"ORI\", \"nombre\": \"Orientación\","
            + " \"descripcion\": \"Información\", \"activo\": true,"
            + " \"horaApertura\": \"08:00:00\", \"horaCierre\": \"17:00:00\"}";

    @Test
    void reemplazarServicioResponde200ConElServicioActualizado() throws Exception {
        when(servicioService.reemplazar(eq(1L), any(ActualizarServicioRequest.class)))
                .thenReturn(new ServicioResponse(1L, "ORI", "Orientación", "Información",
                        LocalTime.of(8, 0), LocalTime.of(17, 0)));

        mockMvc.perform(put("/api/servicios/1").contentType(MediaType.APPLICATION_JSON).content(CUERPO_PUT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("ORI"))
                .andExpect(jsonPath("$.nombre").value("Orientación"));
    }

    @Test
    void reemplazarServicioInexistenteResponde404() throws Exception {
        when(servicioService.reemplazar(eq(9L), any(ActualizarServicioRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("No existe el servicio 9"));

        mockMvc.perform(put("/api/servicios/9").contentType(MediaType.APPLICATION_JSON).content(CUERPO_PUT))
                .andExpect(status().isNotFound());
    }

    @Test
    void reemplazarServicioConCodigoRepetidoResponde409() throws Exception {
        when(servicioService.reemplazar(eq(1L), any(ActualizarServicioRequest.class)))
                .thenThrow(new ReglaNegocioException("Ya existe un servicio con el código ORI"));

        mockMvc.perform(put("/api/servicios/1").contentType(MediaType.APPLICATION_JSON).content(CUERPO_PUT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe un servicio con el código ORI"));
    }
}
