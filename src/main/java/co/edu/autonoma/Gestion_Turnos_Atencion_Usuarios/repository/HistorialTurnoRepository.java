package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;

public interface HistorialTurnoRepository extends JpaRepository<HistorialTurnoEntity, Long> {

    @Query("select new co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.EventoHistorial(h.turno.id, h.estadoNuevo, h.fechaEvento)"
            + " from HistorialTurnoEntity h"
            + " where h.turno.fechaOperativa = :fecha")
    List<EventoHistorial> buscarEventosPorFechaOperativa(@Param("fecha") LocalDate fecha);
}
