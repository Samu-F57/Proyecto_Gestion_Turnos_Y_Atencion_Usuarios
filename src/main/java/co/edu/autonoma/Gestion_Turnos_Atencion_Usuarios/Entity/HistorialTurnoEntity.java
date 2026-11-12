package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.EstadoTurno;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "historial_turnos")
@Getter
@Setter
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

    @PrePersist
    void antesDeInsertar() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
