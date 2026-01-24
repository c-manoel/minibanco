package com.cmanoel.minibanco.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmanoel.minibanco.domain.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
