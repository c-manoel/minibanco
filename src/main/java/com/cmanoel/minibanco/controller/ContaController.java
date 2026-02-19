package com.cmanoel.minibanco.controller;

import java.math.BigDecimal;
import java.util.List;

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
import com.cmanoel.minibanco.dto.AlterarSenhaRequest;
import com.cmanoel.minibanco.dto.CadastrarChavePixRequest;
import com.cmanoel.minibanco.dto.ChavePixResponse;
import com.cmanoel.minibanco.dto.ContaResponse;
import com.cmanoel.minibanco.dto.CriarContaRequest;
import com.cmanoel.minibanco.dto.DepositoRequest;
import com.cmanoel.minibanco.dto.ExtratoItemResponse;
import com.cmanoel.minibanco.dto.OperacaoResponse;
import com.cmanoel.minibanco.dto.PerfilResponse;
import com.cmanoel.minibanco.dto.PixConfirmRequest;
import com.cmanoel.minibanco.dto.PixPreviewRequest;
import com.cmanoel.minibanco.dto.PixPreviewResponse;
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
    public ContaResponse criarConta(@Valid @RequestBody CriarContaRequest request) {
        Conta criada = contaService.criarConta(request);
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

    @GetMapping("/extrato")
    public ResponseEntity<List<ExtratoItemResponse>> extrato(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(contaService.buscarExtrato(email));
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponse> perfil(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(contaService.buscarPerfil(email));
    }

    @PostMapping("/perfil/senha")
    public ResponseEntity<OperacaoResponse> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            Authentication auth) {
        String email = auth.getName();
        contaService.alterarSenha(email, request.getSenhaAtual(), request.getNovaSenha());
        return ResponseEntity.ok(new OperacaoResponse("Senha alterada com sucesso"));
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

    @PostMapping("/pix/chaves")
    public ResponseEntity<ChavePixResponse> cadastrarChavePix(
            @Valid @RequestBody CadastrarChavePixRequest request,
            Authentication auth) {
        String email = auth.getName();
        ChavePixResponse response = contaService.cadastrarChavePix(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/pix/chaves")
    public ResponseEntity<List<ChavePixResponse>> listarChavesPix(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(contaService.listarChavesPix(email));
    }

    @PostMapping("/pix/preview")
    public ResponseEntity<PixPreviewResponse> pixPreview(
            @Valid @RequestBody PixPreviewRequest request,
            Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(contaService.gerarPixPreview(email, request));
    }

    @PostMapping("/pix/confirmar")
    public ResponseEntity<OperacaoResponse> pixConfirmar(
            @Valid @RequestBody PixConfirmRequest request,
            Authentication auth) {
        String email = auth.getName();
        contaService.confirmarPix(email, request);
        return ResponseEntity.ok(new OperacaoResponse("PIX confirmado e realizado com sucesso"));
    }

}
