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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
public class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servicio_id", nullable = false)
    private ServicioEntity servicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private SolicitanteEntity solicitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prioridad_id", nullable = false)
    private PrioridadEntity prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizado_por_id")
    private UsuarioInternoEntity autorizadoPor;

    @Column(name = "fecha_operativa", nullable = false)
    private LocalDate fechaOperativa;

    @Column(nullable = false)
    private int consecutivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTurno estado = EstadoTurno.EN_ESPERA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id")
    private ModuloEntity modulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private UsuarioInternoEntity agente;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public ServicioEntity getServicio() {
        return servicio;
    }

    public void setServicio(ServicioEntity servicio) {
        this.servicio = servicio;
    }

    public SolicitanteEntity getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(SolicitanteEntity solicitante) {
        this.solicitante = solicitante;
    }

    public PrioridadEntity getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadEntity prioridad) {
        this.prioridad = prioridad;
    }

    public UsuarioInternoEntity getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(UsuarioInternoEntity autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public LocalDate getFechaOperativa() {
        return fechaOperativa;
    }

    public void setFechaOperativa(LocalDate fechaOperativa) {
        this.fechaOperativa = fechaOperativa;
    }

    public int getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(int consecutivo) {
        this.consecutivo = consecutivo;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public ModuloEntity getModulo() {
        return modulo;
    }

    public void setModulo(ModuloEntity modulo) {
        this.modulo = modulo;
    }

    public UsuarioInternoEntity getAgente() {
        return agente;
    }

    public void setAgente(UsuarioInternoEntity agente) {
        this.agente = agente;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    @PrePersist
    void antesDeInsertar() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }

}
