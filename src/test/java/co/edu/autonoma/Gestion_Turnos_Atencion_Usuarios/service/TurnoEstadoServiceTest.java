package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@ExtendWith(MockitoExtension.class)
class TurnoEstadoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 5, 9, 0);

    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private UsuarioInternoRepository usuarioRepository;
    @Mock
    private HistorialTurnoRepository historialRepository;

    private TurnoEstadoService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        servicio = new TurnoEstadoService(turnoRepository, usuarioRepository, historialRepository, reloj);
    }

    private TurnoEntity turno(EstadoTurno estado) {
        TurnoEntity t = new TurnoEntity();
        t.setId(70L);
        t.setCodigo("ORI-261005-007");
        t.setEstado(estado);
        return t;
    }

    private HistorialTurnoEntity eventoGuardado() {
        ArgumentCaptor<HistorialTurnoEntity> captor = ArgumentCaptor.forClass(HistorialTurnoEntity.class);
        verify(historialRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void iniciarLaAtencionDeUnTurnoLlamadoLoPasaAEnAtencion() {
        UsuarioInternoEntity agente = new UsuarioInternoEntity();
        agente.setId(2L);
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.LLAMADO)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(agente));

        TurnoEstadoResponse respuesta = servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.EN_ATENCION, 2L, null));

        assertThat(respuesta).isEqualTo(new TurnoEstadoResponse(70L, "ORI-261005-007", EstadoTurno.EN_ATENCION));
        HistorialTurnoEntity evento = eventoGuardado();
        assertThat(evento.getEstadoAnterior()).isEqualTo(EstadoTurno.LLAMADO);
        assertThat(evento.getEstadoNuevo()).isEqualTo(EstadoTurno.EN_ATENCION);
        assertThat(evento.getUsuario().getId()).isEqualTo(2L);
        assertThat(evento.getFechaEvento()).isEqualTo(AHORA);
    }

    @Test
    void finalizarUnTurnoEnAtencionLoPasaAFinalizado() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.EN_ATENCION)));

        TurnoEstadoResponse respuesta = servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.FINALIZADO, null, "Atención completa"));

        assertThat(respuesta.estado()).isEqualTo(EstadoTurno.FINALIZADO);
        assertThat(eventoGuardado().getObservacion()).isEqualTo("Atención completa");
    }

    @Test
    void marcarNoPresenteUnTurnoLlamadoEsValido() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.LLAMADO)));

        assertThat(servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.NO_PRESENTE, null, null)).estado())
                .isEqualTo(EstadoTurno.NO_PRESENTE);
    }

    @Test
    void unTurnoFinalizadoNoVuelveAEnEspera() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.FINALIZADO)));

        assertThatThrownBy(() -> servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.EN_ESPERA, null, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Transición no permitida");
        verify(historialRepository, never()).save(any(HistorialTurnoEntity.class));
    }

    @Test
    void noSePuedeIniciarLaAtencionSinLlamarElTurno() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.EN_ESPERA)));

        assertThatThrownBy(() -> servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.EN_ATENCION, null, null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void noSePuedeLlamarUnTurnoPorEsteEndpoint() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.EN_ESPERA)));

        assertThatThrownBy(() -> servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.LLAMADO, null, null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cambiarElEstadoDeUnTurnoInexistenteLanzaRecursoNoEncontrado() {
        when(turnoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiarEstado(9L,
                new CambiarEstadoTurnoRequest(EstadoTurno.FINALIZADO, null, null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cambiarElEstadoConUnUsuarioInexistenteLanzaRecursoNoEncontrado() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.LLAMADO)));
        when(usuarioRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiarEstado(70L,
                new CambiarEstadoTurnoRequest(EstadoTurno.EN_ATENCION, 8L, null)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("usuario");
    }

    @Test
    void cambiarElEstadoSinEstadoLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> servicio.cambiarEstado(70L, new CambiarEstadoTurnoRequest(null, null, null)))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("estado");
    }

    @Test
    void cancelarUnTurnoEnEsperaLoDejaCanceladoYRegistraElHistorial() {
        TurnoEntity t = turno(EstadoTurno.EN_ESPERA);
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(t));

        servicio.cancelar(70L);

        assertThat(t.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
        HistorialTurnoEntity evento = eventoGuardado();
        assertThat(evento.getEstadoAnterior()).isEqualTo(EstadoTurno.EN_ESPERA);
        assertThat(evento.getEstadoNuevo()).isEqualTo(EstadoTurno.CANCELADO);
    }

    @Test
    void cancelarUnTurnoLlamadoEsValidoPorqueAunNoInicioLaAtencion() {
        TurnoEntity t = turno(EstadoTurno.LLAMADO);
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(t));

        servicio.cancelar(70L);

        assertThat(t.getEstado()).isEqualTo(EstadoTurno.CANCELADO);
    }

    @Test
    void noSePuedeCancelarUnTurnoEnAtencion() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.EN_ATENCION)));

        assertThatThrownBy(() -> servicio.cancelar(70L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("antes de iniciar");
        verify(historialRepository, never()).save(any(HistorialTurnoEntity.class));
    }

    @Test
    void noSePuedeCancelarUnTurnoFinalizado() {
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno(EstadoTurno.FINALIZADO)));

        assertThatThrownBy(() -> servicio.cancelar(70L)).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void cancelarUnTurnoInexistenteLanzaRecursoNoEncontrado() {
        when(turnoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cancelar(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
