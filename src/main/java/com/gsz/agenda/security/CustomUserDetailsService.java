package com.gsz.agenda.security;

import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.repositories.ClienteRepository;
import com.gsz.agenda.repositories.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando usuário por email: {}", email);

        // 1. Tenta encontrar como CLIENTE
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();

            if (!cliente.getAtivo()) {
                log.warn("Cliente inativo: {}", email);
                throw new UsernameNotFoundException("Usuário inativo: " + email);
            }

            List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_CLIENTE")
            );

            return CustomUserDetails.builder()
                    .id(cliente.getId())
                    .email(cliente.getEmail())
                    .senha(cliente.getSenhaHash())
                    .nome(cliente.getNome())
                    .ativo(cliente.getAtivo())
                    .authorities(authorities)
                    .build();
        }

        // 2. Tenta encontrar como PROFISSIONAL
        Optional<Profissional> profissionalOpt = profissionalRepository.findByEmail(email);
        if (profissionalOpt.isPresent()) {
            Profissional profissional = profissionalOpt.get();

            if (!profissional.getAtivo()) {
                log.warn("Profissional inativo: {}", email);
                throw new UsernameNotFoundException("Usuário inativo: " + email);
            }

            List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
            );

            return CustomUserDetails.builder()
                    .id(profissional.getId())
                    .email(profissional.getEmail())
                    .senha(profissional.getSenhaHash())
                    .nome(profissional.getNome())
                    .ativo(profissional.getAtivo())
                    .authorities(authorities)
                    .build();
        }

        log.warn("Usuário não encontrado: {}", email);
        throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
    }
}