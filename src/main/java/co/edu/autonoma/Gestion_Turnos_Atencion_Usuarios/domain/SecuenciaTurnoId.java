package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class SecuenciaTurnoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "servicio_id")
    private Long servicioId;

    @Column(name = "fecha_operativa")
    private LocalDate fechaOperativa;

    public SecuenciaTurnoId() {
    }

    public SecuenciaTurnoId(Long servicioId, LocalDate fechaOperativa) {
        this.servicioId = servicioId;
        this.fechaOperativa = fechaOperativa;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public LocalDate getFechaOperativa() {
        return fechaOperativa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SecuenciaTurnoId otro)) {
            return false;
        }
        return Objects.equals(servicioId, otro.servicioId)
                && Objects.equals(fechaOperativa, otro.fechaOperativa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicioId, fechaOperativa);
    }
}
