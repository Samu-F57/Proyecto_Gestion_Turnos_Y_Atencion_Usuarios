package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ActualizarServicioRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioService servicioService;

    private ServicioEntity servicio(Long id, String codigo, String nombre) {
        ServicioEntity s = new ServicioEntity();
        s.setId(id);
        s.setCodigo(codigo);
        s.setNombre(nombre);
        s.setDescripcion("Descripción de " + nombre);
        s.setActivo(true);
        s.setHoraApertura(LocalTime.of(8, 0));
        s.setHoraCierre(LocalTime.of(17, 0));
        return s;
    }

    @Test
    void listarActivosDevuelveLosServiciosActivosConSuHorario() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc())
                .thenReturn(List.of(servicio(1L, "ORI", "Orientación"),
                        servicio(2L, "SOP", "Soporte")));

        List<ServicioResponse> respuesta = servicioService.listarActivos();

        assertThat(respuesta).hasSize(2);
        assertThat(respuesta.get(0)).isEqualTo(new ServicioResponse(
                1L, "ORI", "Orientación", "Descripción de Orientación",
                LocalTime.of(8, 0), LocalTime.of(17, 0)));
        assertThat(respuesta.get(1).codigo()).isEqualTo("SOP");
    }

    @Test
    void listarActivosSinServiciosDevuelveListaVacia() {
        when(servicioRepository.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of());

        assertThat(servicioService.listarActivos()).isEmpty();
    }

    private ActualizarServicioRequest solicitud(String codigo, String nombre, LocalTime apertura, LocalTime cierre) {
        return new ActualizarServicioRequest(codigo, nombre, "Nueva descripción", false, apertura, cierre);
    }

    @Test
    void reemplazarActualizaTodosLosCamposDelServicio() {
        ServicioEntity existente = servicio(1L, "ORI", "Orientación");
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(servicioRepository.existsByCodigoAndIdNot("INF", 1L)).thenReturn(false);
        when(servicioRepository.existsByNombreAndIdNot("Información", 1L)).thenReturn(false);
        when(servicioRepository.save(any(ServicioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicioResponse respuesta = servicioService.reemplazar(1L,
                solicitud("INF", "Información", LocalTime.of(9, 0), LocalTime.of(15, 30)));

        assertThat(respuesta).isEqualTo(new ServicioResponse(
                1L, "INF", "Información", "Nueva descripción", LocalTime.of(9, 0), LocalTime.of(15, 30)));
        assertThat(existente.isActivo()).isFalse();
    }

    @Test
    void reemplazarConCodigoDeOtroServicioLanzaReglaDeNegocio() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio(1L, "ORI", "Orientación")));
        when(servicioRepository.existsByCodigoAndIdNot("SOP", 1L)).thenReturn(true);

        assertThatThrownBy(() -> servicioService.reemplazar(1L,
                solicitud("SOP", "Información", LocalTime.of(8, 0), LocalTime.of(17, 0))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("código");
        verify(servicioRepository, never()).save(any(ServicioEntity.class));
    }

    @Test
    void reemplazarConNombreDeOtroServicioLanzaReglaDeNegocio() {
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio(1L, "ORI", "Orientación")));
        when(servicioRepository.existsByCodigoAndIdNot("ORI", 1L)).thenReturn(false);
        when(servicioRepository.existsByNombreAndIdNot("Soporte", 1L)).thenReturn(true);

        assertThatThrownBy(() -> servicioService.reemplazar(1L,
                solicitud("ORI", "Soporte", LocalTime.of(8, 0), LocalTime.of(17, 0))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void reemplazarUnServicioInexistenteLanzaRecursoNoEncontrado() {
        when(servicioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.reemplazar(9L,
                solicitud("ORI", "Orientación", LocalTime.of(8, 0), LocalTime.of(17, 0))))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void reemplazarConHorarioInvalidoLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> servicioService.reemplazar(1L,
                solicitud("ORI", "Orientación", LocalTime.of(17, 0), LocalTime.of(8, 0))))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("horaCierre");
    }

    @Test
    void reemplazarConCodigoEnMinusculasLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> servicioService.reemplazar(1L,
                solicitud("ori", "Orientación", LocalTime.of(8, 0), LocalTime.of(17, 0))))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("codigo");
    }

    @Test
    void reemplazarSinNombreLanzaSolicitudInvalida() {
        assertThatThrownBy(() -> servicioService.reemplazar(1L,
                solicitud("ORI", " ", LocalTime.of(8, 0), LocalTime.of(17, 0))))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("nombre");
    }
}
