package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ModuloEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoModulo;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.RolUsuario;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.CambiarEstadoModuloRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ModuloEstadoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ModuloRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.UsuarioInternoRepository;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloRepository moduloRepository;
    @Mock
    private UsuarioInternoRepository usuarioRepository;
    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private ModuloService moduloService;

    private ModuloEntity modulo(EstadoModulo estado, UsuarioInternoEntity agente) {
        ModuloEntity m = new ModuloEntity();
        m.setId(1L);
        m.setCodigo("M1");
        m.setEstado(estado);
        m.setAgente(agente);
        return m;
    }

    private UsuarioInternoEntity usuario(Long id, RolUsuario rol, boolean activo) {
        UsuarioInternoEntity u = new UsuarioInternoEntity();
        u.setId(id);
        u.setNombreCompleto("Agente Uno");
        u.setRol(rol);
        u.setActivo(activo);
        return u;
    }

    @Test
    void abrirUnModuloCerradoLoAsignaAlAgente() {
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L, RolUsuario.AGENTE, true)));
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ModuloEstadoResponse respuesta = moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.ABIERTO, 2L));

        assertThat(respuesta).isEqualTo(new ModuloEstadoResponse(1L, "M1", EstadoModulo.ABIERTO, "Agente Uno"));
    }

    @Test
    void abrirSinAgenteNiAgentePreviaLanzaSolicitudInvalida() {
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null)));

        assertThatThrownBy(() -> moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.ABIERTO, null)))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("agenteId");
    }

    @Test
    void abrirConUsuarioQueNoEsAgenteLanzaReglaDeNegocio() {
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null)));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(usuario(3L, RolUsuario.COORDINADOR, true)));

        assertThatThrownBy(() -> moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.ABIERTO, 3L)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("agente");
        verify(moduloRepository, never()).save(any(ModuloEntity.class));
    }

    @Test
    void reabrirUnModuloPausadoConservaSuAgente() {
        UsuarioInternoEntity agente = usuario(2L, RolUsuario.AGENTE, true);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.PAUSADO, agente)));
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ModuloEstadoResponse respuesta = moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.ABIERTO, null));

        assertThat(respuesta.estado()).isEqualTo(EstadoModulo.ABIERTO);
        assertThat(respuesta.agente()).isEqualTo("Agente Uno");
    }

    @Test
    void pausarUnModuloAbiertoLoDejaPausado() {
        UsuarioInternoEntity agente = usuario(2L, RolUsuario.AGENTE, true);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, agente)));
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ModuloEstadoResponse respuesta = moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.PAUSADO, null));

        assertThat(respuesta.estado()).isEqualTo(EstadoModulo.PAUSADO);
    }

    @Test
    void pausarUnModuloCerradoLanzaReglaDeNegocio() {
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null)));

        assertThatThrownBy(() -> moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.PAUSADO, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("abierto");
    }

    @Test
    void cerrarSinTurnoEnCursoLiberaAlAgente() {
        UsuarioInternoEntity agente = usuario(2L, RolUsuario.AGENTE, true);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, agente)));
        when(turnoRepository.existsByModuloIdAndEstadoIn(1L,
                List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))).thenReturn(false);
        when(moduloRepository.save(any(ModuloEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ModuloEstadoResponse respuesta = moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.CERRADO, null));

        assertThat(respuesta.estado()).isEqualTo(EstadoModulo.CERRADO);
        assertThat(respuesta.agente()).isNull();
    }

    @Test
    void cerrarConTurnoEnCursoLanzaReglaDeNegocio() {
        UsuarioInternoEntity agente = usuario(2L, RolUsuario.AGENTE, true);
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.ABIERTO, agente)));
        when(turnoRepository.existsByModuloIdAndEstadoIn(1L,
                List.of(EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION))).thenReturn(true);

        assertThatThrownBy(() -> moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.CERRADO, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("en curso");
        verify(moduloRepository, never()).save(any(ModuloEntity.class));
    }

    @Test
    void pedirElMismoEstadoLanzaReglaDeNegocio() {
        when(moduloRepository.findById(1L)).thenReturn(Optional.of(modulo(EstadoModulo.CERRADO, null)));

        assertThatThrownBy(() -> moduloService.cambiarEstado(1L,
                new CambiarEstadoModuloRequest(EstadoModulo.CERRADO, null)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ya está");
    }

    @Test
    void moduloInexistenteLanzaRecursoNoEncontrado() {
        when(moduloRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloService.cambiarEstado(9L,
                new CambiarEstadoModuloRequest(EstadoModulo.ABIERTO, 2L)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("9");
    }

    @Test
    void estadoNuloLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> moduloService.cambiarEstado(1L, new CambiarEstadoModuloRequest(null, null)))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("estado");
    }
}
