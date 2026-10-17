package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RazaRepository extends JpaRepository<Raza, Integer> {

    List<Raza> findAllByOrderByNombreAsc();

    Optional<Raza> findByNombre(String nombre);

    Optional<Raza> findByNombreIgnoreCase(String nombre);

    List<Raza> findAllByEspecieOrderByNombreAsc(String especie);

    boolean existsByEspecie(String especie);
}