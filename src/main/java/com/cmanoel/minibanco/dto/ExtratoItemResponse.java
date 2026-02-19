package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExtratoItemResponse {

    private final String tipo;
    private final BigDecimal valor;
    private final LocalDateTime dataHora;
    private final String emailOrigem;
    private final String emailDestino;

    public ExtratoItemResponse(
            String tipo,
            BigDecimal valor,
            LocalDateTime dataHora,
            String emailOrigem,
            String emailDestino) {
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
        this.emailOrigem = emailOrigem;
        this.emailDestino = emailDestino;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getEmailOrigem() {
        return emailOrigem;
    }

    public String getEmailDestino() {
        return emailDestino;
    }
}
