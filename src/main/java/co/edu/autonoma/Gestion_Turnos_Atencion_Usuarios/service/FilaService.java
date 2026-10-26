package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.FilaTurnoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@Service
public class FilaService {

    private final ServicioRepository servicioRepository;
    private final TurnoRepository turnoRepository;
    private final Clock clock;

    public FilaService(ServicioRepository servicioRepository,
            TurnoRepository turnoRepository, Clock clock) {
        this.servicioRepository = servicioRepository;
        this.turnoRepository = turnoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public FilaResponse consultarFila(Long servicioId) {
        ServicioEntity servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el servicio " + servicioId));

        LocalDate fechaOperativa = LocalDate.now(clock);
        LocalDateTime ahora = LocalDateTime.now(clock);
        List<TurnoEntity> enEspera = turnoRepository.buscarFila(
                servicioId, fechaOperativa, EstadoTurno.EN_ESPERA);

        List<FilaTurnoResponse> turnos = new ArrayList<>();
        for (int i = 0; i < enEspera.size(); i++) {
            TurnoEntity turno = enEspera.get(i);
            long espera = Math.max(0,
                    Duration.between(turno.getFechaEmision(), ahora).getSeconds());
            turnos.add(new FilaTurnoResponse(
                    i + 1,
                    turno.getCodigo(),
                    turno.getPrioridad().getNombre(),
                    turno.getFechaEmision(),
                    espera
            ));
        }
        return new FilaResponse(servicio.getId(), servicio.getNombre(),
                fechaOperativa, turnos.size(), turnos);
    }
}
