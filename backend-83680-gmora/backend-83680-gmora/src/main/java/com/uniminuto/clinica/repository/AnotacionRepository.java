package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Anotacion;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio de acceso a datos de las anotaciones de historia clínica.
 */
@Repository
public interface AnotacionRepository extends JpaRepository<Anotacion, Long> {

    /**
     * Busca las anotaciones de una historia con filtros opcionales, paginación y orden.
     *
     * @param historiaId historia a la que pertenecen.
     * @param texto      patrón en minúsculas (con %) para descripción, observación,
     *                   diagnóstico/evolución o veterinario.
     * @param desde      fecha-hora mínima (inclusive).
     * @param hasta      fecha-hora máxima (exclusive).
     * @param pageable   página, tamaño y orden.
     * @return página de anotaciones.
     */
    @Query("""
            SELECT a FROM Anotacion a
            WHERE a.historiaClinica.historiaId = :historiaId
              AND (:desde IS NULL OR a.fechaHora >= :desde)
              AND (:hasta IS NULL OR a.fechaHora < :hasta)
              AND (:texto IS NULL
                   OR LOWER(a.descripcion) LIKE :texto
                   OR LOWER(a.observacion) LIKE :texto
                   OR LOWER(a.diagnosticoEvolucion) LIKE :texto
                   OR LOWER(CONCAT(a.medico.nombres, ' ', a.medico.apellidos)) LIKE :texto)
            """)
    Page<Anotacion> buscarPorHistoria(@Param("historiaId") Long historiaId,
                                      @Param("texto") String texto,
                                      @Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta,
                                      Pageable pageable);

    /**
     * Busca una anotación verificando que pertenezca a la historia indicada.
     *
     * @param anotacionId identificador de la anotación.
     * @param historiaId  identificador de la historia.
     * @return la anotación, si existe y pertenece a esa historia.
     */
    @Query("SELECT a FROM Anotacion a WHERE a.anotacionId = :anotacionId AND a.historiaClinica.historiaId = :historiaId")
    Optional<Anotacion> buscarDeHistoria(@Param("anotacionId") Long anotacionId,
                                         @Param("historiaId") Long historiaId);

    /**
     * @param medico veterinario a revisar.
     * @return {@code true} si el veterinario escribió anotaciones (impide eliminarlo).
     */
    boolean existsByMedico(Medico medico);
}
