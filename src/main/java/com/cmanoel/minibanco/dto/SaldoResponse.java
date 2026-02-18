package com.cmanoel.minibanco.dto;

import java.math.BigDecimal;

public class SaldoResponse {

    private final BigDecimal saldo;

    public SaldoResponse(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
