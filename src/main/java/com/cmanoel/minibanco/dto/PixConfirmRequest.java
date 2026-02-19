package com.cmanoel.minibanco.dto;

import jakarta.validation.constraints.NotBlank;

public class PixConfirmRequest {

    @NotBlank(message = "Número da operação é obrigatório")
    private String numeroOperacao;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public String getNumeroOperacao() {
        return numeroOperacao;
    }

    public void setNumeroOperacao(String numeroOperacao) {
        this.numeroOperacao = numeroOperacao;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
