package com.cmanoel.minibanco.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.repository.ContaRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    public Conta criarConta(Conta conta) {
        conta.setSaldo(BigDecimal.ZERO);
        return contaRepository.save(conta);
    }
}