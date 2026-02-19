package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PixPreviewResponse {

    private final String numeroOperacao;
    private final BigDecimal valor;
    private final String destinoNome;
    private final String destinoCpfMascarado;
    private final String destinoChaveMascarada;
    private final LocalDateTime expiraEm;

    public PixPreviewResponse(
            String numeroOperacao,
            BigDecimal valor,
            String destinoNome,
            String destinoCpfMascarado,
            String destinoChaveMascarada,
            LocalDateTime expiraEm) {
        this.numeroOperacao = numeroOperacao;
        this.valor = valor;
        this.destinoNome = destinoNome;
        this.destinoCpfMascarado = destinoCpfMascarado;
        this.destinoChaveMascarada = destinoChaveMascarada;
        this.expiraEm = expiraEm;
    }

    public String getNumeroOperacao() {
        return numeroOperacao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDestinoNome() {
        return destinoNome;
    }

    public String getDestinoCpfMascarado() {
        return destinoCpfMascarado;
    }

    public String getDestinoChaveMascarada() {
        return destinoChaveMascarada;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}
