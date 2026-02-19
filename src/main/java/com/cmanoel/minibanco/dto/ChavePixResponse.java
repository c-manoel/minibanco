package com.cmanoel.minibanco.dto;

public class ChavePixResponse {

    private final Long id;
    private final String tipo;
    private final String chaveMascarada;

    public ChavePixResponse(Long id, String tipo, String chaveMascarada) {
        this.id = id;
        this.tipo = tipo;
        this.chaveMascarada = chaveMascarada;
    }

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getChaveMascarada() {
        return chaveMascarada;
    }
}
