package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.UsuarioInternoEntity;

public interface UsuarioInternoRepository extends JpaRepository<UsuarioInternoEntity, Long> {
}
