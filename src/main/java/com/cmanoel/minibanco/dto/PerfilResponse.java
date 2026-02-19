package com.cmanoel.minibanco.dto;

public class PerfilResponse {

    private final String nome;
    private final String email;
    private final String cpfMascarado;

    public PerfilResponse(String nome, String email, String cpfMascarado) {
        this.nome = nome;
        this.email = email;
        this.cpfMascarado = cpfMascarado;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpfMascarado() {
        return cpfMascarado;
    }
}
