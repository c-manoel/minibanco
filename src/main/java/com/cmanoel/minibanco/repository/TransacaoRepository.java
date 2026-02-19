package com.cmanoel.minibanco.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmanoel.minibanco.domain.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByOrigemEmailOrDestinoEmailOrderByDataDesc(String origemEmail, String destinoEmail);

    List<Transacao> findByOrigemEmailAndTipoAndDataBetween(
        String origemEmail,
        String tipo,
        LocalDateTime inicio,
        LocalDateTime fim
    );
}
