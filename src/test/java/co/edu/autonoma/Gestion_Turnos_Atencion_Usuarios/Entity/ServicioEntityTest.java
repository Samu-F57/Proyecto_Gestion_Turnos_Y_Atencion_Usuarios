package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class ServicioEntityTest {

    @Test
    void nuevoServicio_sinDatos_empiezaActivo() {
        // Arrange y Act
        ServicioEntity servicio = new ServicioEntity();

        // Assert
        assertThat(servicio.isActivo()).isTrue();
    }

    @Test
    void antesDeInsertar_servicioNuevo_asignaLasDosFechasActuales() {
        // Arrange
        ServicioEntity servicio = new ServicioEntity();

        // Act
        servicio.antesDeInsertar();

        // Assert
        LocalDateTime ahora = LocalDateTime.now();
        assertThat(servicio.getFechaCreacion()).isCloseTo(ahora, within(5, ChronoUnit.SECONDS));
        assertThat(servicio.getFechaActualizacion()).isCloseTo(ahora, within(5, ChronoUnit.SECONDS));
    }

    @Test
    void antesDeActualizar_servicioExistente_refrescaSoloLaFechaDeActualizacion() {
        // Arrange
        LocalDateTime antigua = LocalDateTime.of(2020, 1, 1, 8, 0);
        ServicioEntity servicio = new ServicioEntity();
        servicio.setFechaCreacion(antigua);
        servicio.setFechaActualizacion(antigua);

        // Act
        servicio.antesDeActualizar();

        // Assert
        assertThat(servicio.getFechaCreacion()).isEqualTo(antigua);
        assertThat(servicio.getFechaActualizacion())
                .isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }
}
