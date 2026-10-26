package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EmitirTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEmitidoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.EmisionTurnoService;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service.TurnoService;

@WebMvcTest(TurnoController.class)
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;

    @MockitoBean
    private EmisionTurnoService emisionTurnoService;

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

    private static final String CUERPO = "{\"servicioId\": 1, \"solicitante\": {\"tipoDocumento\": \"CC\","
            + " \"numeroDocumento\": \"1020304050\", \"nombreCompleto\": \"Ana Pérez\"}}";

    @Test
    void emitirTurnoResponde201ConElTurnoCreado() throws Exception {
        when(emisionTurnoService.emitir(any(EmitirTurnoRequest.class))).thenReturn(new TurnoEmitidoResponse(
                7L, "ORI-261005-007", "Orientación", EstadoTurno.EN_ESPERA,
                LocalDate.of(2026, 10, 5), 7, LocalDateTime.of(2026, 10, 5, 8, 0)));

        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.codigo").value("ORI-261005-007"))
                .andExpect(jsonPath("$.estado").value("EN_ESPERA"))
                .andExpect(jsonPath("$.consecutivo").value(7));
    }

    @Test
    void emitirTurnoConReglaDeNegocioIncumplidaResponde409() throws Exception {
        when(emisionTurnoService.emitir(any(EmitirTurnoRequest.class)))
                .thenThrow(new ReglaNegocioException("El solicitante ya tiene un turno activo"));

        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409))
                .andExpect(jsonPath("$.mensaje").value("El solicitante ya tiene un turno activo"));
    }

    @Test
    void emitirTurnoConDatosInvalidosResponde400() throws Exception {
        when(emisionTurnoService.emitir(any(EmitirTurnoRequest.class)))
                .thenThrow(new SolicitudInvalidaException("servicioId es obligatorio"));

        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("servicioId es obligatorio"));
    }

    @Test
    void emitirTurnoConJsonMalFormadoResponde400() throws Exception {
        mockMvc.perform(post("/api/turnos").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest());
    }
}
