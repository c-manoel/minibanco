package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PixPreviewRequest {

    @NotBlank(message = "Chave PIX de destino é obrigatória")
    private String chaveDestino;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    private boolean confirmacaoExtra;

    public String getChaveDestino() {
        return chaveDestino;
    }

    public void setChaveDestino(String chaveDestino) {
        this.chaveDestino = chaveDestino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public boolean isConfirmacaoExtra() {
        return confirmacaoExtra;
    }

    public void setConfirmacaoExtra(boolean confirmacaoExtra) {
        this.confirmacaoExtra = confirmacaoExtra;
    }
}
