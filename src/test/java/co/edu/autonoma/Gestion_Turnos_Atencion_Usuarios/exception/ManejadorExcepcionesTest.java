package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ErrorResponse;

class ManejadorExcepcionesTest {

    private final ManejadorExcepciones manejador = new ManejadorExcepciones();

    @Test
    void manejarNoEncontrado_conExcepcion_respondeEstado404ConElMensaje() {
        // Arrange
        RecursoNoEncontradoException error = new RecursoNoEncontradoException("No existe el turno XXX");

        // Act
        ErrorResponse respuesta = manejador.manejarNoEncontrado(error);

        // Assert
        assertThat(respuesta.estado()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(respuesta.mensaje()).isEqualTo("No existe el turno XXX");
    }

    @Test
    void manejarReglaNegocio_conExcepcion_respondeEstado409() {
        // Arrange
        ReglaNegocioException error = new ReglaNegocioException("Ya tiene un turno activo");

        // Act
        ErrorResponse respuesta = manejador.manejarReglaNegocio(error);

        // Assert
        assertThat(respuesta.estado()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(respuesta.mensaje()).isEqualTo("Ya tiene un turno activo");
    }

    @Test
    void manejarSolicitudInvalida_conExcepcion_respondeEstado400() {
        // Arrange
        SolicitudInvalidaException error = new SolicitudInvalidaException("servicioId es obligatorio");

        // Act
        ErrorResponse respuesta = manejador.manejarSolicitudInvalida(error);

        // Assert
        assertThat(respuesta.estado()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(respuesta.mensaje()).isEqualTo("servicioId es obligatorio");
    }
}
