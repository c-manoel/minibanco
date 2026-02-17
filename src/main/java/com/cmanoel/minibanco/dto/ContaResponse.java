package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;

public class ContaResponse {

    private Long id;
    private String nome;
    private String email;
    private BigDecimal saldo;

    public ContaResponse(Long id, String nome, String email, BigDecimal saldo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
