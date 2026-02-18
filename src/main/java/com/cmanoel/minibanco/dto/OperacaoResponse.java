package com.cmanoel.minibanco.dto;

public class OperacaoResponse {

    private final String mensagem;

    public OperacaoResponse(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
}
