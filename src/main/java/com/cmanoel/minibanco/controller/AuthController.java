package com.cmanoel.minibanco.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.dto.LoginRequest;
import com.cmanoel.minibanco.repository.ContaRepository;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(ContaRepository contaRepository,
                          PasswordEncoder passwordEncoder) {
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Conta conta = contaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (!passwordEncoder.matches(request.getSenha(), conta.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Senha inválida");
        }

        return ResponseEntity.ok("Login realizado com sucesso");
    }
}
