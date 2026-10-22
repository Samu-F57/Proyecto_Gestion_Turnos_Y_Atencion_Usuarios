package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.PrioridadEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class FilaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 5);

    @Mock
    private ServicioRepository servicioRepository;

    @Mock
    private TurnoRepository turnoRepository;

    private FilaService filaService;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(
                LocalDateTime.of(2026, 10, 5, 8, 30).atZone(ZONA).toInstant(), ZONA);
        filaService = new FilaService(servicioRepository, turnoRepository, reloj);
    }

    private TurnoEntity turno(String codigo, String prioridad, LocalDateTime emision) {
        PrioridadEntity p = new PrioridadEntity();
        p.setNombre(prioridad);
        TurnoEntity t = new TurnoEntity();
        t.setCodigo(codigo);
        t.setPrioridad(p);
        t.setEstado(EstadoTurno.EN_ESPERA);
        t.setFechaEmision(emision);
        return t;
    }

    @Test
    void consultarFilaNumeraLosTurnosYCalculaElTiempoDeEspera() {
        ServicioEntity servicio = new ServicioEntity();
        servicio.setId(1L);
        servicio.setNombre("Orientación");
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(turnoRepository.buscarFila(1L, HOY, EstadoTurno.EN_ESPERA)).thenReturn(List.of(
                turno("ORI-261005-002", "PREFERENCIAL", LocalDateTime.of(2026, 10, 5, 8, 10)),
                turno("ORI-261005-001", "NORMAL", LocalDateTime.of(2026, 10, 5, 8, 0))));

        FilaResponse fila = filaService.consultarFila(1L);

        assertThat(fila.servicioId()).isEqualTo(1L);
        assertThat(fila.servicio()).isEqualTo("Orientación");
        assertThat(fila.fechaOperativa()).isEqualTo(HOY);
        assertThat(fila.totalEnEspera()).isEqualTo(2);
        assertThat(fila.turnos()).hasSize(2);
        assertThat(fila.turnos().get(0).orden()).isEqualTo(1);
        assertThat(fila.turnos().get(0).codigo()).isEqualTo("ORI-261005-002");
        assertThat(fila.turnos().get(0).prioridad()).isEqualTo("PREFERENCIAL");
        assertThat(fila.turnos().get(0).tiempoEsperaSegundos()).isEqualTo(1200);
        assertThat(fila.turnos().get(1).orden()).isEqualTo(2);
        assertThat(fila.turnos().get(1).tiempoEsperaSegundos()).isEqualTo(1800);
    }

    @Test
    void consultarFilaVaciaDevuelveListaVacia() {
        ServicioEntity servicio = new ServicioEntity();
        servicio.setId(1L);
        servicio.setNombre("Orientación");
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(turnoRepository.buscarFila(1L, HOY, EstadoTurno.EN_ESPERA)).thenReturn(List.of());

        FilaResponse fila = filaService.consultarFila(1L);

        assertThat(fila.totalEnEspera()).isZero();
        assertThat(fila.turnos()).isEmpty();
        verify(turnoRepository).buscarFila(1L, HOY, EstadoTurno.EN_ESPERA);
    }

    @Test
    void consultarFilaDeServicioInexistenteLanzaRecursoNoEncontrado() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filaService.consultarFila(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }
}
