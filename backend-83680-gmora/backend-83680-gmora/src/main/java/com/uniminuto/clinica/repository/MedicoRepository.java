package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    List<Medico> findAllByOrderByNombresAsc();

    Optional<Medico> findByNumeroDocumentoIgnoreCase(String numeroDocumento);

    List<Medico> findAllByEspecializacionOrderByNombresAsc(Especializacion especializacion);
}