package com.cmanoel.minibanco.security;

import org.springframework.stereotype.Service;

@Service
public class ScoreService {

    private static final int SCORE_MINIMO_APROVACAO = 500;

    public int calcularScore(String cpf) {
        int somaPonderada = 0;
        for (int i = 0; i < cpf.length(); i++) {
            int digito = Character.getNumericValue(cpf.charAt(i));
            somaPonderada += digito * (i + 3);
        }

        int score = (somaPonderada * 17) % 1000;
        if (score < 200) {
            score += 200;
        }
        return score;
    }

    public boolean scoreAprovado(int score) {
        return score >= SCORE_MINIMO_APROVACAO;
    }
}
