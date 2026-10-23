package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.SecuenciaTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.SecuenciaTurnoId;

public interface SecuenciaTurnoRepository extends JpaRepository<SecuenciaTurnoEntity, SecuenciaTurnoId> {

    // RN-01: siguiente consecutivo por servicio y fecha operativa, atómico en PostgreSQL
    @Query(value = "insert into secuencias_turno (servicio_id, fecha_operativa, ultimo_consecutivo)"
            + " values (:servicioId, :fecha, 1)"
            + " on conflict (servicio_id, fecha_operativa)"
            + " do update set ultimo_consecutivo = secuencias_turno.ultimo_consecutivo + 1"
            + " returning ultimo_consecutivo", nativeQuery = true)
    int siguienteConsecutivo(@Param("servicioId") Long servicioId, @Param("fecha") LocalDate fecha);
}
