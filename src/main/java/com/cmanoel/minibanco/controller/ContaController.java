package com.cmanoel.minibanco.controller;

import java.math.BigDecimal;

import jakarta.validation.Valid;

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
import com.cmanoel.minibanco.dto.ContaResponse;
import com.cmanoel.minibanco.dto.DepositoRequest;
import com.cmanoel.minibanco.dto.OperacaoResponse;
import com.cmanoel.minibanco.dto.PixRequest;
import com.cmanoel.minibanco.dto.SaldoResponse;
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
    public ContaResponse criarConta(@RequestBody Conta conta) {
        Conta criada = contaService.criarConta(conta);
        return new ContaResponse(
            criada.getId(),
            criada.getNome(),
            criada.getEmail(),
            criada.getSaldo()
        );
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoResponse> verSaldo(Authentication auth) {
        String email = auth.getName();
        BigDecimal saldo = contaService.buscarSaldo(email);
        return ResponseEntity.ok(new SaldoResponse(saldo));
    }

    @PostMapping("/deposito")
    public ResponseEntity<OperacaoResponse> depositar(
            @Valid @RequestBody DepositoRequest request,
            Authentication auth) {
        String email = auth.getName();
        contaService.depositar(email, request.getValor());
        return ResponseEntity.ok(new OperacaoResponse("Depósito realizado com sucesso"));
    }

    @PostMapping("/pix")
    public ResponseEntity<OperacaoResponse> pix(
            @Valid @RequestBody PixRequest request,
            Authentication auth) {

        String emailOrigem = auth.getName();
        contaService.realizarPix(emailOrigem, request);

        return ResponseEntity.ok(new OperacaoResponse("PIX realizado com sucesso"));
    }

}
