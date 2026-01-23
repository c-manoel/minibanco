package com.cmanoel.minibanco.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class GeradorSenha {

    @Bean
    CommandLineRunner gerarSenha(PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Senha criptografada:");
            System.out.println(passwordEncoder.encode("123456"));
        };
    }
}
