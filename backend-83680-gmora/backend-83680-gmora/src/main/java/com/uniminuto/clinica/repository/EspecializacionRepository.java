package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion, Long> {

    List<Especializacion> findAllByOrderByNombreAsc();

    Optional<Especializacion> findByNombreIgnoreCase(String nombre);

    Optional<Especializacion> findByCodigoEspecializacionIgnoreCase(String codigoEspecializacion);
}