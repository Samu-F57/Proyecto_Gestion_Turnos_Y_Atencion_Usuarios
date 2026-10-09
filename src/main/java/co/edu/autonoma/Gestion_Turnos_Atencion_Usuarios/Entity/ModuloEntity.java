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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "modulos")
public class ModuloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoModulo estado = EstadoModulo.CERRADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_id")
    private UsuarioInternoEntity agente;

    @ManyToMany
    @JoinTable(
            name = "modulo_servicio",
            joinColumns = @JoinColumn(name = "modulo_id"),
            inverseJoinColumns = @JoinColumn(name = "servicio_id"))
    private Set<ServicioEntity> servicios = new HashSet<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoModulo getEstado() {
        return estado;
    }

    public void setEstado(EstadoModulo estado) {
        this.estado = estado;
    }

    public UsuarioInternoEntity getAgente() {
        return agente;
    }

    public void setAgente(UsuarioInternoEntity agente) {
        this.agente = agente;
    }

    public Set<ServicioEntity> getServicios() {
        return servicios;
    }

    public void setServicios(Set<ServicioEntity> servicios) {
        this.servicios = servicios;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @PrePersist
    void antesDeInsertar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

}
