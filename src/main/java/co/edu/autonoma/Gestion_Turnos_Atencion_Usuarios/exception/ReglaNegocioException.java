package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.exception;

public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
