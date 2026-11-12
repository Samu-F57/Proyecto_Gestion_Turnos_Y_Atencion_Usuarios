package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;

public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {

    @Query("select t from TurnoEntity t"
            + " join fetch t.servicio"
            + " left join fetch t.modulo"
            + " where t.codigo = :codigo")
    Optional<TurnoEntity> buscarPorCodigo(@Param("codigo") String codigo);

    // RN-08: prioridad (mayor nivel primero) y, dentro de ella, orden de emisión
    @Query("select t from TurnoEntity t"
            + " join fetch t.prioridad p"
            + " where t.servicio.id = :servicioId"
            + " and t.fechaOperativa = :fecha"
            + " and t.estado = :estado"
            + " order by p.nivel desc, t.fechaEmision asc, t.consecutivo asc")
    List<TurnoEntity> buscarFila(@Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoTurno estado);

    long countByFechaOperativaAndEstado(LocalDate fecha, EstadoTurno estado);

    // RN-03: un solicitante no puede tener dos turnos activos para el mismo servicio
    boolean existsBySolicitanteIdAndServicioIdAndEstadoIn(
            Long solicitanteId, Long servicioId, Collection<EstadoTurno> estados);

    // RN-06 y RN-08: turnos en espera de los servicios habilitados, por prioridad y emisión
    @Query("select t from TurnoEntity t"
            + " join fetch t.prioridad p"
            + " join fetch t.servicio s"
            + " where s.id in :servicioIds"
            + " and t.fechaOperativa = :fecha"
            + " and t.estado = :estado"
            + " order by p.nivel desc, t.fechaEmision asc, t.consecutivo asc")
    List<TurnoEntity> buscarElegibles(@Param("servicioIds") Collection<Long> servicioIds,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoTurno estado,
            Pageable pageable);

    // Bloquea la fila para que dos módulos no llamen el mismo turno
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TurnoEntity t where t.id = :id")
    Optional<TurnoEntity> bloquearPorId(@Param("id") Long id);

    // RN-07: un módulo no puede tener dos atenciones activas
    boolean existsByModuloIdAndEstadoIn(Long moduloId, Collection<EstadoTurno> estados);
}
