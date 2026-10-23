package co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.Entity.SolicitanteEntity;
import co.edu.autonoma.Gestion_Turnos_Atencion_Usuarios.domain.TipoDocumento;

public interface SolicitanteRepository extends JpaRepository<SolicitanteEntity, Long> {

    Optional<SolicitanteEntity> findByTipoDocumentoAndNumeroDocumento(
            TipoDocumento tipoDocumento, String numeroDocumento);
}
