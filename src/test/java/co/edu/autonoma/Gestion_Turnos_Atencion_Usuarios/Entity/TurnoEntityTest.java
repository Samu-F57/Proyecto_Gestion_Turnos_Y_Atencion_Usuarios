package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

class TurnoEntityTest {

    @Test
    void nuevoTurno_sinDatos_empiezaEnEspera() {
        // Arrange y Act
        TurnoEntity turno = new TurnoEntity();

        // Assert
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.EN_ESPERA);
    }

    @Test
    void antesDeInsertar_sinFechaEmision_asignaLaFechaActual() {
        // Arrange
        TurnoEntity turno = new TurnoEntity();

        // Act
        turno.antesDeInsertar();

        // Assert
        assertThat(turno.getFechaEmision())
                .isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    void antesDeInsertar_conFechaEmision_laConserva() {
        // Arrange
        LocalDateTime emision = LocalDateTime.of(2026, 10, 5, 8, 0);
        TurnoEntity turno = new TurnoEntity();
        turno.setFechaEmision(emision);

        // Act
        turno.antesDeInsertar();

        // Assert
        assertThat(turno.getFechaEmision()).isEqualTo(emision);
    }

    @Test
    void setters_conDatos_losExponenLosGettersDeLombok() {
        // Arrange
        TurnoEntity turno = new TurnoEntity();

        // Act
        turno.setCodigo("ORI-261005-007");
        turno.setConsecutivo(7);
        turno.setEstado(EstadoTurno.LLAMADO);

        // Assert
        assertThat(turno.getCodigo()).isEqualTo("ORI-261005-007");
        assertThat(turno.getConsecutivo()).isEqualTo(7);
        assertThat(turno.getEstado()).isEqualTo(EstadoTurno.LLAMADO);
    }
}
