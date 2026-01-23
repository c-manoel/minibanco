package com.cmanoel.minibanco.service;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.repository.ContaRepository;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(ContaRepository contaRepository,
                        PasswordEncoder passwordEncoder) {
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Conta criarConta(Conta conta) {
        conta.setSenha(passwordEncoder.encode(conta.getSenha()));
        conta.setSaldo(BigDecimal.ZERO);
        return contaRepository.save(conta);
    }
}
