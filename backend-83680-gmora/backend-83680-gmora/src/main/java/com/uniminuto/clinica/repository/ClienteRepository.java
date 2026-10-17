package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByNombresAsc();

    Optional<Cliente> findByNumeroDocumento(String numeroDocumento);

    Optional<Cliente> findByNumeroDocumentoIgnoreCase(String numeroDocumento);
}