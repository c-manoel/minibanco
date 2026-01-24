package com.cmanoel.minibanco.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.dto.PixRequest;
import com.cmanoel.minibanco.service.ContaService;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Conta criarConta(@RequestBody Conta conta) {
        return contaService.criarConta(conta);
    }

    @GetMapping("/saldo")
public ResponseEntity<BigDecimal> verSaldo(Authentication auth) {
    String email = auth.getName();
    BigDecimal saldo = contaService.buscarSaldo(email);
    return ResponseEntity.ok(saldo);
    }

@PostMapping("/pix")
public ResponseEntity<String> pix(
        @RequestBody PixRequest request,
        Authentication auth) {

    String emailOrigem = auth.getName();
    contaService.realizarPix(emailOrigem, request);

    return ResponseEntity.ok("PIX realizado com sucesso");
    }

}
