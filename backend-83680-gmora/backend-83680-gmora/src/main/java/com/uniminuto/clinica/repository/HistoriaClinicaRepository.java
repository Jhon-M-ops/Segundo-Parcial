package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.HistoriaClinica;
import com.uniminuto.clinica.entity.Mascota;
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
 * Repositorio de acceso a datos de las historias clínicas.
 */
@Repository
public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    /**
     * Busca historias con filtros opcionales, paginación y ordenamiento.
     * Cada filtro se ignora cuando llega nulo.
     *
     * @param texto    patrón en minúsculas (con %) para mascota, cliente, veterinario o motivo.
     * @param medicoId id del veterinario.
     * @param estado   estado exacto (ABIERTA o CERRADA).
     * @param desde    fecha-hora mínima (inclusive).
     * @param hasta    fecha-hora máxima (exclusive).
     * @param pageable página, tamaño y orden.
     * @return página de historias.
     */
    @Query("""
            SELECT h FROM HistoriaClinica h
            WHERE (:medicoId IS NULL OR h.medico.id = :medicoId)
              AND (:estado IS NULL OR h.estado = :estado)
              AND (:desde IS NULL OR h.fechaHora >= :desde)
              AND (:hasta IS NULL OR h.fechaHora < :hasta)
              AND (:texto IS NULL
                   OR LOWER(h.mascota.nombreMascota) LIKE :texto
                   OR LOWER(CONCAT(h.mascota.cliente.nombres, ' ', h.mascota.cliente.apellidos)) LIKE :texto
                   OR LOWER(CONCAT(h.medico.nombres, ' ', h.medico.apellidos)) LIKE :texto
                   OR LOWER(h.motivo) LIKE :texto)
            """)
    Page<HistoriaClinica> buscar(@Param("texto") String texto,
                                 @Param("medicoId") Long medicoId,
                                 @Param("estado") String estado,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta,
                                 Pageable pageable);

    /**
     * Busca la historia generada por una cita.
     *
     * @param cita cita de origen.
     * @return la historia, si existe.
     */
    Optional<HistoriaClinica> findByCita(Cita cita);

    /**
     * @param cita cita de origen.
     * @return {@code true} si la cita ya generó una historia (evita duplicados).
     */
    boolean existsByCita(Cita cita);

    /**
     * @param mascota mascota a revisar.
     * @return {@code true} si la mascota tiene historias (impide eliminarla).
     */
    boolean existsByMascota(Mascota mascota);

    /**
     * @param medico veterinario a revisar.
     * @return {@code true} si el veterinario tiene historias (impide eliminarlo).
     */
    boolean existsByMedico(Medico medico);
}
