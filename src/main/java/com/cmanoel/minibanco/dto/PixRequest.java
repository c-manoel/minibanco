package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;

public class PixRequest {

    private String emailDestino;
    private BigDecimal valor;

    public String getEmailDestino() {
        return emailDestino;
    }

    public void setEmailDestino(String emailDestino) {
        this.emailDestino = emailDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
