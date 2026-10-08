package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "secuencias_turno")
public class SecuenciaTurnoEntity {

    @EmbeddedId
    private SecuenciaTurnoId id;

    @Column(name = "ultimo_consecutivo", nullable = false)
    private int ultimoConsecutivo;

    public SecuenciaTurnoId getId() {
        return id;
    }

    public void setId(SecuenciaTurnoId id) {
        this.id = id;
    }

    public int getUltimoConsecutivo() {
        return ultimoConsecutivo;
    }

    public void setUltimoConsecutivo(int ultimoConsecutivo) {
        this.ultimoConsecutivo = ultimoConsecutivo;
    }
}
