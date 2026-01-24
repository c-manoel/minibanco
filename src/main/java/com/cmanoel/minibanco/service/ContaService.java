package com.cmanoel.minibanco.service;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.dto.PixRequest;
import com.cmanoel.minibanco.repository.ContaRepository;

import jakarta.transaction.Transactional;

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

    public BigDecimal buscarSaldo(String email) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        return conta.getSaldo();
    }

    @Transactional
    public void realizarPix(String emailOrigem, PixRequest request) {

        Conta origem = contaRepository.findByEmail(emailOrigem)
            .orElseThrow(() -> new RuntimeException("Conta origem não encontrada"));

        Conta destino = contaRepository.findByEmail(request.getEmailDestino())
            .orElseThrow(() -> new RuntimeException("Conta destino não encontrada"));

        BigDecimal valor = request.getValor();

        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));

        contaRepository.save(origem);
        contaRepository.save(destino);
    }
}
