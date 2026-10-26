package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.ServicioResponse;
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
}
