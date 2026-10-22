package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;

public interface ServicioRepository extends JpaRepository<ServicioEntity, Long> {

    List<ServicioEntity> findByActivoTrueOrderByNombreAsc();
}
