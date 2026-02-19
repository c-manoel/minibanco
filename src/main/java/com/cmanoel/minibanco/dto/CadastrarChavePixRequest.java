package com.cmanoel.minibanco.dto;

import jakarta.validation.constraints.NotBlank;

public class CadastrarChavePixRequest {

    @NotBlank(message = "Tipo da chave é obrigatório")
    private String tipo;

    @NotBlank(message = "Valor da chave é obrigatório")
    private String chave;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }
}
