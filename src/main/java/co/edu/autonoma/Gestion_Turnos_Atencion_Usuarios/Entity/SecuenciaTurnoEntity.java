package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "secuencias_turno")
@Getter
@Setter
public class SecuenciaTurnoEntity {

    @EmbeddedId
    private SecuenciaTurnoId id;

    @Column(name = "ultimo_consecutivo", nullable = false)
    private int ultimoConsecutivo;
}
