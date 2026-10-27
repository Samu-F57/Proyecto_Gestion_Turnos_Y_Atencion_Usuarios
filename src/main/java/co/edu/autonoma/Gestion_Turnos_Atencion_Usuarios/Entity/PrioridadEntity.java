package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prioridades")
@Getter
@Setter
public class PrioridadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private int nivel;

    @Column(name = "requiere_autorizacion", nullable = false)
    private boolean requiereAutorizacion = true;

    @Column(nullable = false)
    private boolean activa = true;
}
