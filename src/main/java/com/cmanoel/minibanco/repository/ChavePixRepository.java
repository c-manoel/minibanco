package com.cmanoel.minibanco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmanoel.minibanco.domain.ChavePix;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {

    Optional<ChavePix> findByChave(String chave);

    List<ChavePix> findByContaEmailOrderByIdDesc(String email);
}
