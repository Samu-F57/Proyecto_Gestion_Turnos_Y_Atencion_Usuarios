package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_turnos")
public class HistorialTurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id", nullable = false, updatable = false)
    private TurnoEntity turno;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", updatable = false, length = 15)
    private EstadoTurno estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, updatable = false, length = 15)
    private EstadoTurno estadoNuevo;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", updatable = false)
    private ModuloEntity modulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", updatable = false)
    private UsuarioInternoEntity usuario;

    @Column(length = 255, updatable = false)
    private String observacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TurnoEntity getTurno() {
        return turno;
    }

    public void setTurno(TurnoEntity turno) {
        this.turno = turno;
    }

    public EstadoTurno getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoTurno estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoTurno getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoTurno estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public ModuloEntity getModulo() {
        return modulo;
    }

    public void setModulo(ModuloEntity modulo) {
        this.modulo = modulo;
    }

    public UsuarioInternoEntity getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioInternoEntity usuario) {
        this.usuario = usuario;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @PrePersist
    void antesDeInsertar() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }

}
