package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ModuloEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.RolUsuario;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.LlamarSiguienteTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoLlamadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ModuloRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@ExtendWith(MockitoExtension.class)
class LlamadoTurnoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 5);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 5, 8, 30);

    @Mock
    private ModuloRepository moduloRepository;
    @Mock
    private UsuarioInternoRepository usuarioRepository;
    @Mock
    private TurnoRepository turnoRepository;
    @Mock
    private HistorialTurnoRepository historialRepository;

    private LlamadoTurnoService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(AHORA.atZone(ZONA).toInstant(), ZONA);
        servicio = new LlamadoTurnoService(moduloRepository, usuarioRepository,
                turnoRepository, historialRepository, reloj);
    }

    private UsuarioInternoEntity agente(Long id, RolUsuario rol) {
        UsuarioInternoEntity u = new UsuarioInternoEntity();
        u.setId(id);
        u.setNombreCompleto("Agente Uno");
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }

    private ModuloEntity modulo(EstadoModulo estado, UsuarioInternoEntity agente, Long... servicios) {
        ModuloEntity m = new ModuloEntity();
        m.setId(1L);
        m.setNombre("Módulo 1");
        m.setEstado(estado);
        m.setAgente(agente);
        for (Long id : servicios) {
            ServicioEntity s = new ServicioEntity();
            s.setId(id);
            m.getServicios().add(s);
        }
        return m;
    }

    private TurnoEntity turno(EstadoTurno estado) {
        TurnoEntity t = new TurnoEntity();
        t.setId(70L);
        t.setCodigo("ORI-261005-007");
        t.setEstado(estado);
        return t;
    }

    private void moduloAbiertoConAgente() {
        UsuarioInternoEntity a = agente(2L, RolUsuario.AGENTE);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, a, 1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(a));
        when(turnoRepository.existsByModuloIdAndEstadoIn(1L,
                List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))).thenReturn(false);
    }

    @Test
    void llamarSiguienteAsignaModuloYAgenteYRegistraElHistorial() {
        moduloAbiertoConAgente();
        TurnoEntity elegido = turno(EstadoTurno.EN_ESPERA);
        when(turnoRepository.buscarElegibles(eq(List.of(1L)), eq(HOY), eq(EstadoTurno.EN_ESPERA),
                any(Pageable.class))).thenReturn(List.of(elegido));
        when(turnoRepository.bloquearPorId(70L)).thenReturn(Optional.of(elegido));

        TurnoLlamadoResponse respuesta = servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L));

        assertThat(respuesta).isEqualTo(new TurnoLlamadoResponse(
                "ORI-261005-007", EstadoTurno.LLAMADO, "Módulo 1", "Agente Uno", AHORA));
        assertThat(elegido.getModulo().getId()).isEqualTo(1L);
        assertThat(elegido.getAgente().getId()).isEqualTo(2L);

        ArgumentCaptor<HistorialTurnoEntity> evento = ArgumentCaptor.forClass(HistorialTurnoEntity.class);
        verify(historialRepository).save(evento.capture());
        assertThat(evento.getValue().getEstadoAnterior()).isEqualTo(EstadoTurno.EN_ESPERA);
        assertThat(evento.getValue().getEstadoNuevo()).isEqualTo(EstadoTurno.LLAMADO);
        assertThat(evento.getValue().getUsuario().getId()).isEqualTo(2L);
        assertThat(evento.getValue().getFechaEvento()).isEqualTo(AHORA);
    }

    @Test
    void llamarSiguienteSinTurnosEnEsperaLanzaRecursoNoEncontrado() {
        moduloAbiertoConAgente();
        when(turnoRepository.buscarElegibles(eq(List.of(1L)), eq(HOY), eq(EstadoTurno.EN_ESPERA),
                any(Pageable.class))).thenReturn(List.of());

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No hay turnos");
    }

    @Test
    void llamarSiguienteRechazaUnTurnoQueOtroModuloYaLlamo() {
        moduloAbiertoConAgente();
        when(turnoRepository.buscarElegibles(eq(List.of(1L)), eq(HOY), eq(EstadoTurno.EN_ESPERA),
                any(Pageable.class))).thenReturn(List.of(turno(EstadoTurno.EN_ESPERA)));
        when(turnoRepository.bloquearPorId(70L)).thenReturn(Optional.of(turno(EstadoTurno.LLAMADO)));

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("otro módulo");
        verify(historialRepository, never()).save(any(HistorialTurnoEntity.class));
    }

    @Test
    void llamarSiguienteConElModuloCerradoLanzaReglaDeNegocio() {
        UsuarioInternoEntity a = agente(2L, RolUsuario.AGENTE);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null, 1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(a));

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está abierto");
    }

    @Test
    void llamarSiguienteConUnAgenteDistintoAlDelModuloLanzaReglaDeNegocio() {
        UsuarioInternoEntity duenio = agente(5L, RolUsuario.AGENTE);
        UsuarioInternoEntity otro = agente(2L, RolUsuario.AGENTE);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, duenio, 1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("otro agente");
    }

    @Test
    void llamarSiguienteConUsuarioQueNoEsAgenteLanzaReglaDeNegocio() {
        UsuarioInternoEntity coordinador = agente(2L, RolUsuario.COORDINADOR);
        when(moduloRepository.findById(1L)).thenReturn(
                Optional.of(modulo(EstadoModulo.ABIERTO, coordinador, 1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(coordinador));

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("agente activo");
    }

    @Test
    void llamarSiguienteConUnTurnoEnCursoEnElModuloLanzaReglaDeNegocio() {
        UsuarioInternoEntity a = agente(2L, RolUsuario.AGENTE);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, a, 1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(a));
        when(turnoRepository.existsByModuloIdAndEstadoIn(1L,
                List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))).thenReturn(true);

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("en curso");
    }

    @Test
    void llamarSiguienteSinServiciosHabilitadosLanzaReglaDeNegocio() {
        UsuarioInternoEntity a = agente(2L, RolUsuario.AGENTE);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, a)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(a));
        when(turnoRepository.existsByModuloIdAndEstadoIn(1L,
                List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))).thenReturn(false);

        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("servicios habilitados");
    }

    @Test
    void llamarSiguienteSinAgenteIdLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> servicio.llamarSiguiente(1L, new LlamarSiguienteTurnoRequest(null)))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("agenteId");
    }

    @Test
    void llamarSiguienteEnUnModuloInexistenteLanzaRecursoNoEncontrado() {
        when(moduloRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.llamarSiguiente(9L, new LlamarSiguienteTurnoRequest(2L)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("módulo");
    }
}
