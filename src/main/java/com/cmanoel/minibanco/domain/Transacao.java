package com.cmanoel.minibanco.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "origem_id")
    private Conta origem;

    @ManyToOne
    @JoinColumn(name = "destino_id")
    private Conta destino;

    private Double valor;

    private LocalDateTime data;

    public Transacao() {
        this.data = LocalDateTime.now();
    }

    // getters e setters
}
