package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.IndicadoresResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.EventoHistorial;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@Service
public class IndicadorService {

    private final TurnoRepository turnoRepository;
    private final HistorialTurnoRepository historialRepository;

    public IndicadorService(TurnoRepository turnoRepository,
            HistorialTurnoRepository historialRepository) {
        this.turnoRepository = turnoRepository;
        this.historialRepository = historialRepository;
    }

    // RN-12: los tiempos se calculan desde las fechas del historial
    @Transactional(readOnly = true)
    public IndicadoresResponse calcular(LocalDate fecha) {
        long atendidos = turnoRepository.countByFechaOperativaAndEstado(
                fecha, EstadoTurno.FINALIZADO);

        Map<Long, Map<EstadoTurno, LocalDateTime>> porTurno = new HashMap<>();
        for (EventoHistorial evento : historialRepository.buscarEventosPorFechaOperativa(fecha)) {
            porTurno.computeIfAbsent(evento.turnoId(), id -> new EnumMap<>(EstadoTurno.class))
                    .merge(evento.estado(), evento.fecha(),
                            (a, b) -> a.isBefore(b) ? a : b);
        }

        List<Double> esperas = new ArrayList<>();
        List<Double> atenciones = new ArrayList<>();
        for (Map<EstadoTurno, LocalDateTime> eventos : porTurno.values()) {
            agregarDuracion(esperas, eventos.get(EstadoTurno.EN_ESPERA),
                    eventos.get(EstadoTurno.LLAMADO));
            agregarDuracion(atenciones, eventos.get(EstadoTurno.EN_ATENCION),
                    eventos.get(EstadoTurno.FINALIZADO));
        }
        return new IndicadoresResponse(fecha, atendidos, promedio(esperas), promedio(atenciones));
    }

    private void agregarDuracion(List<Double> destino, LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null) {
            destino.add(Duration.between(inicio, fin).toMillis() / 1000.0);
        }
    }

    private Double promedio(List<Double> valores) {
        return valores.isEmpty() ? null
                : valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
