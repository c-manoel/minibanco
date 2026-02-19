package com.cmanoel.minibanco.security;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {

    public boolean isValid(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int digito1 = calcularDigito(cpf, 9, 10);
        int digito2 = calcularDigito(cpf, 10, 11);

        return digito1 == Character.getNumericValue(cpf.charAt(9))
            && digito2 == Character.getNumericValue(cpf.charAt(10));
    }

    private int calcularDigito(String cpf, int limite, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;

        for (int i = 0; i < limite; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso;
            peso--;
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
