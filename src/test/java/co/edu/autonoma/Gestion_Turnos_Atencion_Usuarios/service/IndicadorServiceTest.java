package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.IndicadoresResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.EventoHistorial;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class IndicadorServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 5);

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private HistorialTurnoRepository historialRepository;

    @InjectMocks
    private IndicadorService indicadorService;

    private EventoHistorial evento(Long turno, EstadoTurno estado, int hora, int minuto) {
        return new EventoHistorial(turno, estado, LocalDateTime.of(2026, 10, 5, hora, minuto));
    }

    @Test
    void calcularPromediaEsperaYAtencionDesdeElHistorial() {
        when(turnoRepository.countByFechaOperativaAndEstado(FECHA, EstadoTurno.FINALIZADO))
                .thenReturn(1L);
        when(historialRepository.buscarEventosPorFechaOperativa(FECHA)).thenReturn(List.of(
                // turno 1: espera 10 min (600 s), atención 18 min (1080 s)
                evento(1L, EstadoTurno.EN_ESPERA, 8, 0),
                evento(1L, EstadoTurno.LLAMADO, 8, 10),
                evento(1L, EstadoTurno.EN_ATENCION, 8, 12),
                evento(1L, EstadoTurno.FINALIZADO, 8, 30),
                // turno 2: espera 15 min (900 s), aún sin atención
                evento(2L, EstadoTurno.EN_ESPERA, 8, 5),
                evento(2L, EstadoTurno.LLAMADO, 8, 20)));

        IndicadoresResponse respuesta = indicadorService.calcular(FECHA);

        assertThat(respuesta.fecha()).isEqualTo(FECHA);
        assertThat(respuesta.cantidadAtendida()).isEqualTo(1);
        assertThat(respuesta.tiempoPromedioEsperaSegundos()).isEqualTo(750.0);
        assertThat(respuesta.tiempoPromedioAtencionSegundos()).isEqualTo(1080.0);
    }

    @Test
    void calcularSinActividadDevuelveCeroYPromediosNulos() {
        when(turnoRepository.countByFechaOperativaAndEstado(FECHA, EstadoTurno.FINALIZADO))
                .thenReturn(0L);
        when(historialRepository.buscarEventosPorFechaOperativa(FECHA)).thenReturn(List.of());

        IndicadoresResponse respuesta = indicadorService.calcular(FECHA);

        assertThat(respuesta.cantidadAtendida()).isZero();
        assertThat(respuesta.tiempoPromedioEsperaSegundos()).isNull();
        assertThat(respuesta.tiempoPromedioAtencionSegundos()).isNull();
    }
}
