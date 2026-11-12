package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoModuloRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.LlamarSiguienteTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ModuloEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoLlamadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.LlamadoTurnoService;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.ModuloService;

@WebMvcTest(ModuloController.class)
class ModuloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModuloService moduloService;

    @MockitoBean
    private LlamadoTurnoService llamadoTurnoService;

    @Test
    void cambiarEstadoDelModuloResponde200() throws Exception {
        when(moduloService.cambiarEstado(eq(1L), any(CambiarEstadoModuloRequest.class)))
                .thenReturn(new ModuloEstadoResponse(1L, "M1", EstadoModulo.ABIERTO, "Agente Uno"));

        mockMvc.perform(patch("/api/modulos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ABIERTO\", \"agenteId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("M1"))
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.agente").value("Agente Uno"));
    }

    @Test
    void cambiarEstadoConTransicionInvalidaResponde409() throws Exception {
        when(moduloService.cambiarEstado(eq(1L), any(CambiarEstadoModuloRequest.class)))
                .thenThrow(new ReglaNegocioException("El módulo tiene un turno en curso"));

        mockMvc.perform(patch("/api/modulos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CERRADO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El módulo tiene un turno en curso"));
    }

    @Test
    void cambiarEstadoSinDatosValidosResponde400() throws Exception {
        when(moduloService.cambiarEstado(eq(1L), any(CambiarEstadoModuloRequest.class)))
                .thenThrow(new SolicitudInvalidaException("estado es obligatorio"));

        mockMvc.perform(patch("/api/modulos/1/estado").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarEstadoConEstadoDesconocidoResponde400() throws Exception {
        mockMvc.perform(patch("/api/modulos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"VOLANDO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void llamarSiguienteTurnoResponde200ConElTurnoLlamado() throws Exception {
        when(llamadoTurnoService.llamarSiguiente(eq(1L), any(LlamarSiguienteTurnoRequest.class)))
                .thenReturn(new TurnoLlamadoResponse("ORI-261005-002", EstadoTurno.LLAMADO,
                        "Módulo 1", "Agente Uno", LocalDateTime.of(2026, 10, 5, 8, 30)));

        mockMvc.perform(post("/api/modulos/1/siguiente-turno").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agenteId\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("ORI-261005-002"))
                .andExpect(jsonPath("$.estado").value("LLAMADO"))
                .andExpect(jsonPath("$.modulo").value("Módulo 1"))
                .andExpect(jsonPath("$.agente").value("Agente Uno"));
    }

    @Test
    void llamarSiguienteTurnoSinTurnosEnEsperaResponde404() throws Exception {
        when(llamadoTurnoService.llamarSiguiente(eq(1L), any(LlamarSiguienteTurnoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("No hay turnos en espera"));

        mockMvc.perform(post("/api/modulos/1/siguiente-turno").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agenteId\": 2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No hay turnos en espera"));
    }

    @Test
    void llamarSiguienteTurnoConModuloCerradoResponde409() throws Exception {
        when(llamadoTurnoService.llamarSiguiente(eq(1L), any(LlamarSiguienteTurnoRequest.class)))
                .thenThrow(new ReglaNegocioException("El módulo Módulo 1 no está abierto"));

        mockMvc.perform(post("/api/modulos/1/siguiente-turno").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agenteId\": 2}"))
                .andExpect(status().isConflict());
    }
}
