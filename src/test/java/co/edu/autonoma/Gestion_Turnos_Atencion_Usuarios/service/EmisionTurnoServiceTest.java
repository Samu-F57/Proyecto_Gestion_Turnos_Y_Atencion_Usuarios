package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.PrioridadEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.SolicitanteEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.TipoDocumento;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EmitirTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.SolicitanteRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEmitidoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.PrioridadRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.SecuenciaTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.SolicitanteRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class EmisionTurnoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 5);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 5, 8, 30);

    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private SolicitanteRepository solicitanteRepository;
    @Mock
    private PrioridadRepository prioridadRepository;
    @Mock
    private SecuenciaTurnoRepository secuenciaRepository;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private HistorialTurnoRepository historialRepository;

    private EmisionTurnoService servicio;

    @BeforeEach
    void preparar() {
        servicio = crearServicio(AHORA);
    }

    private EmisionTurnoService crearServicio(LocalDateTime momento) {
        Clock reloj = Clock.fixed(momento.atZone(ZONA).toInstant(), ZONA);
        return new EmisionTurnoService(servicioRepository, solicitanteRepository, prioridadRepository,
                secuenciaRepository, turnoRepository, historialRepository, reloj);
    }

    private ServicioEntity servicioOrientacion(boolean activo) {
        ServicioEntity s = new ServicioEntity();
        s.setId(1L);
        s.setCodigo("ORI");
        s.setNombre("Orientación");
        s.setActivo(activo);
        s.setHoraApertura(LocalTime.of(8, 0));
        s.setHoraCierre(LocalTime.of(17, 0));
        return s;
    }

    private PrioridadEntity prioridad(boolean requiereAutorizacion, boolean activa) {
        PrioridadEntity p = new PrioridadEntity();
        p.setId(1L);
        p.setNombre("NORMAL");
        p.setRequiereAutorizacion(requiereAutorizacion);
        p.setActiva(activa);
        return p;
    }

    private EmitirTurnoRequest solicitud(Long prioridadId) {
        return new EmitirTurnoRequest(1L,
                new SolicitanteRequest(TipoDocumento.CC, "1020304050", "Ana Pérez",
                        "ana@correo.com", "3001234567"),
                prioridadId);
    }

    @Test
    void emitirCreaTurnoYSolicitanteNuevoConCodigoYHistorial() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(true)));
        when(prioridadRepository.findByNombre("NORMAL")).thenReturn(Optional.of(prioridad(false, true)));
        when(solicitanteRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.CC, "1020304050"))
                .thenReturn(Optional.empty());
        when(solicitanteRepository.save(any(SolicitanteEntity.class))).thenAnswer(inv -> {
            SolicitanteEntity s = inv.getArgument(0);
            s.setId(5L);
            return s;
        });
        when(secuenciaRepository.siguienteConsecutivo(1L, HOY)).thenReturn(7);
        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(inv -> {
            TurnoEntity t = inv.getArgument(0);
            t.setId(70L);
            return t;
        });

        TurnoEmitidoResponse respuesta = servicio.emitir(solicitud(null));

        assertThat(respuesta).isEqualTo(new TurnoEmitidoResponse(
                70L, "ORI-261005-007", "Orientación", EstadoTurno.EN_ESPERA, HOY, 7, AHORA));

        ArgumentCaptor<HistorialTurnoEntity> evento = ArgumentCaptor.forClass(HistorialTurnoEntity.class);
        verify(historialRepository).save(evento.capture());
        assertThat(evento.getValue().getEstadoAnterior()).isNull();
        assertThat(evento.getValue().getEstadoNuevo()).isEqualTo(EstadoTurno.EN_ESPERA);
        assertThat(evento.getValue().getFechaEvento()).isEqualTo(AHORA);
    }

    @Test
    void emitirReutilizaAlSolicitanteExistenteSinDuplicarlo() {
        SolicitanteEntity existente = new SolicitanteEntity();
        existente.setId(5L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(true)));
        when(prioridadRepository.findById(1L)).thenReturn(Optional.of(prioridad(false, true)));
        when(solicitanteRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.CC, "1020304050"))
                .thenReturn(Optional.of(existente));
        when(turnoRepository.existsBySolicitanteIdAndServicioIdAndEstadoIn(
                5L, 1L, List.of(EstadoTurno.EN_ESPERA, EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION)))
                .thenReturn(false);
        when(secuenciaRepository.siguienteConsecutivo(1L, HOY)).thenReturn(1);
        when(turnoRepository.save(any(TurnoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        TurnoEmitidoResponse respuesta = servicio.emitir(solicitud(1L));

        assertThat(respuesta.codigo()).isEqualTo("ORI-261005-001");
        verify(solicitanteRepository, never()).save(any(SolicitanteEntity.class));
    }

    @Test
    void emitirRechazaSiElSolicitanteYaTieneTurnoActivoEnElServicio() {
        SolicitanteEntity existente = new SolicitanteEntity();
        existente.setId(5L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(true)));
        when(prioridadRepository.findById(1L)).thenReturn(Optional.of(prioridad(false, true)));
        when(solicitanteRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.CC, "1020304050"))
                .thenReturn(Optional.of(existente));
        when(turnoRepository.existsBySolicitanteIdAndServicioIdAndEstadoIn(
                5L, 1L, List.of(EstadoTurno.EN_ESPERA, EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION)))
                .thenReturn(true);

        assertThatThrownBy(() -> servicio.emitir(solicitud(1L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("turno activo");
        verify(secuenciaRepository, never()).siguienteConsecutivo(anyLong(), any(LocalDate.class));
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void emitirRechazaFueraDelHorarioDelServicio() {
        EmisionTurnoService tarde = crearServicio(LocalDateTime.of(2026, 10, 5, 17, 0));
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(true)));

        assertThatThrownBy(() -> tarde.emitir(solicitud(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("horario");
        verify(turnoRepository, never()).save(any(TurnoEntity.class));
    }

    @Test
    void emitirRechazaServicioInactivo() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(false)));

        assertThatThrownBy(() -> servicio.emitir(solicitud(null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está activo");
    }

    @Test
    void emitirRechazaServicioInexistente() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.emitir(solicitud(null)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("servicio");
    }

    @Test
    void emitirRechazaPrioridadQueRequiereAutorizacion() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioOrientacion(true)));
        when(prioridadRepository.findById(2L)).thenReturn(Optional.of(prioridad(true, true)));

        assertThatThrownBy(() -> servicio.emitir(solicitud(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("autorización");
    }

    @Test
    void emitirRechazaSolicitudSinServicioId() {
        EmitirTurnoRequest sinServicio = new EmitirTurnoRequest(null, solicitud(null).solicitante(), null);

        assertThatThrownBy(() -> servicio.emitir(sinServicio))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("servicioId");
    }

    @Test
    void emitirRechazaSolicitanteSinDocumento() {
        EmitirTurnoRequest sinDocumento = new EmitirTurnoRequest(1L,
                new SolicitanteRequest(TipoDocumento.CC, " ", "Ana Pérez", null, null), null);

        assertThatThrownBy(() -> servicio.emitir(sinDocumento))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("numeroDocumento");
    }
}
