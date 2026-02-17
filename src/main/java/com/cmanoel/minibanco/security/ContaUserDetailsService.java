package com.cmanoel.minibanco.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.repository.ContaRepository;

@Service
public class ContaUserDetailsService implements UserDetailsService {

    private final ContaRepository contaRepository;

    public ContaUserDetailsService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return contaRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
    }
}
