package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ModuloEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoPublicoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    private static final LocalDateTime EMISION = LocalDateTime.of(2026, 10, 5, 8, 0);

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private TurnoService turnoService;

    private TurnoEntity turno(EstadoTurno estado, ModuloEntity modulo) {
        ServicioEntity servicio = new ServicioEntity();
        servicio.setNombre("Orientación");
        TurnoEntity t = new TurnoEntity();
        t.setCodigo("ORI-261005-007");
        t.setServicio(servicio);
        t.setEstado(estado);
        t.setModulo(modulo);
        t.setFechaEmision(EMISION);
        return t;
    }

    private ModuloEntity modulo() {
        ModuloEntity m = new ModuloEntity();
        m.setNombre("Módulo 1");
        return m;
    }

    @Test
    void consultarTurnoLlamadoMuestraElModulo() {
        when(turnoRepository.buscarPorCodigo("ORI-261005-007"))
                .thenReturn(Optional.of(turno(EstadoTurno.LLAMADO, modulo())));

        TurnoPublicoResponse respuesta = turnoService.consultarPorCodigo("ORI-261005-007");

        assertThat(respuesta).isEqualTo(new TurnoPublicoResponse(
                "ORI-261005-007", "Orientación", EstadoTurno.LLAMADO, "Módulo 1", EMISION));
    }

    @Test
    void consultarTurnoEnEsperaNoTieneModulo() {
        when(turnoRepository.buscarPorCodigo("ORI-261005-007"))
                .thenReturn(Optional.of(turno(EstadoTurno.EN_ESPERA, null)));

        TurnoPublicoResponse respuesta = turnoService.consultarPorCodigo("ORI-261005-007");

        assertThat(respuesta.estado()).isEqualTo(EstadoTurno.EN_ESPERA);
        assertThat(respuesta.modulo()).isNull();
    }

    @Test
    void consultarTurnoFinalizadoOcultaElModulo() {
        when(turnoRepository.buscarPorCodigo("ORI-261005-007"))
                .thenReturn(Optional.of(turno(EstadoTurno.FINALIZADO, modulo())));

        assertThat(turnoService.consultarPorCodigo("ORI-261005-007").modulo()).isNull();
    }

    @Test
    void consultarTurnoInexistenteLanzaRecursoNoEncontrado() {
        when(turnoRepository.buscarPorCodigo("XXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> turnoService.consultarPorCodigo("XXX"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("XXX");
    }
}
