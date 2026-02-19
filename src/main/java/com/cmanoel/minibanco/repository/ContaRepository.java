package com.cmanoel.minibanco.repository;

import com.cmanoel.minibanco.domain.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByEmail(String email);

    boolean existsByCpf(String cpf);
}
