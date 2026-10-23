package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.HistorialTurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.PrioridadEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.ServicioEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.SolicitanteEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.TurnoEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.EmitirTurnoRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.SolicitanteRequest;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.dto.TurnoEmitidoResponse;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.ReglaNegocioException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.RecursoNoEncontradoException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception.SolicitudInvalidaException;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.HistorialTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.PrioridadRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.SecuenciaTurnoRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.ServicioRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.SolicitanteRepository;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository.TurnoRepository;

@Service
public class EmisionTurnoService {

    private static final List<EstadoTurno> ESTADOS_ACTIVOS =
            List.of(EstadoTurno.EN_ESPERA, EstadoTurno.LLAMADO, EstadoTurno.EN_ATENCION);
    private static final String PRIORIDAD_POR_DEFECTO = "NORMAL";
    private static final DateTimeFormatter FECHA_CODIGO = DateTimeFormatter.ofPattern("yyMMdd");

    private final ServicioRepository servicioRepository;
    private final SolicitanteRepository solicitanteRepository;
    private final PrioridadRepository prioridadRepository;
    private final SecuenciaTurnoRepository secuenciaRepository;
    private final TurnoRepository turnoRepository;
    private final HistorialTurnoRepository historialRepository;
    private final Clock clock;

    public EmisionTurnoService(ServicioRepository servicioRepository,
            SolicitanteRepository solicitanteRepository,
            PrioridadRepository prioridadRepository,
            SecuenciaTurnoRepository secuenciaRepository,
            TurnoRepository turnoRepository,
            HistorialTurnoRepository historialRepository,
            Clock clock) {
        this.servicioRepository = servicioRepository;
        this.solicitanteRepository = solicitanteRepository;
        this.prioridadRepository = prioridadRepository;
        this.secuenciaRepository = secuenciaRepository;
        this.turnoRepository = turnoRepository;
        this.historialRepository = historialRepository;
        this.clock = clock;
    }

    @Transactional
    public TurnoEmitidoResponse emitir(EmitirTurnoRequest request) {
        validar(request);

        ServicioEntity servicio = servicioRepository.findById(request.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el servicio " + request.servicioId()));
        LocalDateTime ahora = LocalDateTime.now(clock);
        validarServicioDisponible(servicio, ahora.toLocalTime());

        PrioridadEntity prioridad = obtenerPrioridad(request.prioridadId());
        SolicitanteRequest datos = request.solicitante();
        SolicitanteEntity solicitante = solicitanteRepository
                .findByTipoDocumentoAndNumeroDocumento(datos.tipoDocumento(), datos.numeroDocumento())
                .orElse(null);
        if (solicitante == null) {
            solicitante = solicitanteRepository.save(crearSolicitante(datos));
        } else if (turnoRepository.existsBySolicitanteIdAndServicioIdAndEstadoIn(
                solicitante.getId(), servicio.getId(), ESTADOS_ACTIVOS)) {
            throw new ReglaNegocioException(
                    "El solicitante ya tiene un turno activo para el servicio " + servicio.getNombre());
        }

        LocalDate fechaOperativa = ahora.toLocalDate();
        int consecutivo = secuenciaRepository.siguienteConsecutivo(servicio.getId(), fechaOperativa);

        TurnoEntity turno = new TurnoEntity();
        turno.setCodigo(String.format("%s-%s-%03d",
                servicio.getCodigo(), FECHA_CODIGO.format(fechaOperativa), consecutivo));
        turno.setServicio(servicio);
        turno.setSolicitante(solicitante);
        turno.setPrioridad(prioridad);
        turno.setFechaOperativa(fechaOperativa);
        turno.setConsecutivo(consecutivo);
        turno.setEstado(EstadoTurno.EN_ESPERA);
        turno.setFechaEmision(ahora);
        TurnoEntity guardado = turnoRepository.save(turno);

        HistorialTurnoEntity evento = new HistorialTurnoEntity();
        evento.setTurno(guardado);
        evento.setEstadoAnterior(null);
        evento.setEstadoNuevo(EstadoTurno.EN_ESPERA);
        evento.setFechaEvento(ahora);
        historialRepository.save(evento);

        return new TurnoEmitidoResponse(
                guardado.getId(),
                guardado.getCodigo(),
                servicio.getNombre(),
                guardado.getEstado(),
                guardado.getFechaOperativa(),
                guardado.getConsecutivo(),
                guardado.getFechaEmision()
        );
    }

    private void validar(EmitirTurnoRequest request) {
        if (request == null || request.servicioId() == null) {
            throw new SolicitudInvalidaException("servicioId es obligatorio");
        }
        SolicitanteRequest s = request.solicitante();
        if (s == null || s.tipoDocumento() == null || estaVacio(s.numeroDocumento())
                || estaVacio(s.nombreCompleto())) {
            throw new SolicitudInvalidaException(
                    "solicitante requiere tipoDocumento, numeroDocumento y nombreCompleto");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    // RN-02: el servicio debe estar activo y dentro de su horario
    private void validarServicioDisponible(ServicioEntity servicio, LocalTime hora) {
        if (!servicio.isActivo()) {
            throw new ReglaNegocioException("El servicio " + servicio.getNombre() + " no está activo");
        }
        if (hora.isBefore(servicio.getHoraApertura()) || !hora.isBefore(servicio.getHoraCierre())) {
            throw new ReglaNegocioException(
                    "El servicio " + servicio.getNombre() + " está fuera de su horario de atención");
        }
    }

    private PrioridadEntity obtenerPrioridad(Long prioridadId) {
        PrioridadEntity prioridad = prioridadId == null
                ? prioridadRepository.findByNombre(PRIORIDAD_POR_DEFECTO)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No existe la prioridad " + PRIORIDAD_POR_DEFECTO))
                : prioridadRepository.findById(prioridadId)
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "No existe la prioridad " + prioridadId));
        if (!prioridad.isActiva()) {
            throw new ReglaNegocioException("La prioridad " + prioridad.getNombre() + " no está activa");
        }
        if (prioridad.isRequiereAutorizacion()) {
            throw new ReglaNegocioException(
                    "La prioridad " + prioridad.getNombre() + " requiere autorización");
        }
        return prioridad;
    }

    private SolicitanteEntity crearSolicitante(SolicitanteRequest datos) {
        SolicitanteEntity solicitante = new SolicitanteEntity();
        solicitante.setTipoDocumento(datos.tipoDocumento());
        solicitante.setNumeroDocumento(datos.numeroDocumento());
        solicitante.setNombreCompleto(datos.nombreCompleto());
        solicitante.setCorreo(datos.correo());
        solicitante.setTelefono(datos.telefono());
        return solicitante;
    }
}
