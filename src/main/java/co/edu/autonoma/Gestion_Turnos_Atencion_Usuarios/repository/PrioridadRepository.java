package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.PrioridadEntity;

public interface PrioridadRepository extends JpaRepository<PrioridadEntity, Long> {

    Optional<PrioridadEntity> findByNombre(String nombre);
}
