package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception;

public class SolicitudInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
