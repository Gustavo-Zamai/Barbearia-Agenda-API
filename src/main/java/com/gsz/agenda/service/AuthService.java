package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.AlterarSenhaRequest;
import com.gsz.agenda.dto.request.LoginRequest;
import com.gsz.agenda.dto.request.RegistroClienteRequest;
import com.gsz.agenda.dto.response.LoginResponse;
import com.gsz.agenda.enums.Genero;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.repositories.ClienteRepository;
import com.gsz.agenda.repositories.ProfissionalRepository;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;
    private final LogAtividadeService logAtividadeService;

    // Novas dependências no topo da classe
    private final ClienteRepository clienteRepository;
    private final ProfissionalRepository profissionalRepository;

    /**
     * Cadastrar novo cliente
     */
    @Transactional
    public LoginResponse cadastrarCliente(RegistroClienteRequest request) {
        log.info("Cadastro de novo cliente: {}", request.getEmail());

        if (!request.getSenha().equals(request.getConfirmacaoSenha())) {
            throw new BusinessException("Senha e confirmação não coincidem");
        }

        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Já existe um cliente cadastrado com este email");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setEmail(request.getEmail());
        cliente.setTelefone(request.getTelefone());
        cliente.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        cliente.setDataNascimento(request.getDataNascimento());
        cliente.setAtivo(true);

        if (request.getGenero() != null && !request.getGenero().isBlank()) {
            try {
                cliente.setGenero(Genero.valueOf(request.getGenero().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Gênero inválido. Use MASCULINO, FEMININO ou OUTRO");
            }
        }

        cliente = clienteRepository.save(cliente);

        logAtividadeService.salvarLog(
                cliente.getEmail(), "CADASTRO_CLIENTE", "clientes", cliente.getId(), null, null, null, null);

        log.info("Cliente cadastrado com sucesso: {}", cliente.getEmail());

        // Gera token já para login automático
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                cliente.getEmail(), request.getSenha());
        Authentication authenticated = authenticationManager.authenticate(authentication);
        String token = tokenProvider.gerarToken(authenticated);

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiracao(tokenProvider.getExpiracaoToken())
                .usuarioId(cliente.getId())
                .nome(cliente.getNome())
                .email(cliente.getEmail())
                .role("CLIENTE")
                .build();
    }

    // cadastrarProfissional() foi removido: era uma rota pública que dava
    // ROLE_ADMIN a qualquer pessoa. Criação de profissional agora só é
    // possível via ProfissionalService.criar(), chamado por um admin
    // autenticado (POST /api/profissionais).

    /**
     * Autenticar usuário e gerar token JWT
     */
    public LoginResponse autenticar(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getEmail());

        try {
            // 1. Autenticar usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()));

            // 2. Colocar autenticação no contexto
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Buscar cliente
            Cliente cliente = clienteService.buscarClientePorEmail(request.getEmail());

            // 4. Verificar se cliente está ativo
            if (!cliente.getAtivo()) {
                throw new BusinessException("Usuário inativo. Entre em contato com o administrador");
            }

            // 5. Gerar token JWT
            String token = tokenProvider.gerarToken(authentication);
            Long expiracao = tokenProvider.getExpiracaoToken();

            // 6. Registrar log
            logAtividadeService.salvarLog(
                    cliente.getEmail(),
                    "LOGIN_SUCESSO",
                    "auth",
                    cliente.getId(),
                    null,
                    null,
                    null,
                    null);

            log.info("Login realizado com sucesso para: {}", request.getEmail());

            // 7. Retornar resposta
            return LoginResponse.builder()
                    .token(token)
                    .tipo("Bearer")
                    .expiracao(expiracao)
                    .usuarioId(cliente.getId())
                    .nome(cliente.getNome())
                    .email(cliente.getEmail())
                    .role("CLIENTE")
                    .build();

        } catch (Exception e) {
            log.warn("Falha no login para: {}", request.getEmail());

            // Registrar log de falha
            logAtividadeService.salvarLog(
                    request.getEmail(),
                    "LOGIN_FALHA",
                    "auth",
                    null,
                    null,
                    e.getMessage(),
                    null,
                    null);

            throw new BusinessException("Email ou senha inválidos");
        }
    }

    /**
     * Obter usuário logado
     */
    public Cliente getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Usuário não autenticado");
        }

        String email = authentication.getName();
        return clienteService.buscarClientePorEmail(email);
    }

    /**
     * Obter ID do usuário logado
     */
    public Integer getUsuarioLogadoId() {
        return getUsuarioLogado().getId();
    }

    /**
     * Obter email do usuário logado
     */
    public String getUsuarioLogadoEmail() {
        return getUsuarioLogado().getEmail();
    }

    /**
     * Alterar senha do cliente logado
     */
    @Transactional
    public void alterarSenha(AlterarSenhaRequest request) {
        // Buscar o cliente logado
        Cliente cliente = getUsuarioLogado();

        log.info("Alteração de senha para: {}", cliente.getEmail());

        // Verificar se as senhas coincidem
        if (!request.getNovaSenha().equals(request.getConfirmacaoSenha())) {
            throw new BusinessException("Nova senha e confirmação não coincidem");
        }

        // Verificar senha atual
        if (!passwordEncoder.matches(request.getSenhaAtual(), cliente.getSenhaHash())) {
            throw new BusinessException("Senha atual incorreta");
        }

        // Validar nova senha
        if (request.getNovaSenha().length() < 6) {
            throw new BusinessException("Nova senha deve ter pelo menos 6 caracteres");
        }

        // Atualizar senha
        cliente.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        clienteService.atualizarSenha(cliente);

        // Registrar log
        logAtividadeService.salvarLog(
                cliente.getEmail(),
                "ALTERAR_SENHA",
                "clientes",
                cliente.getId(),
                null,
                null,
                null,
                null);

        log.info("Senha alterada com sucesso para: {}", cliente.getEmail());
    }

    /**
     * Logout (invalidação do token)
     */
    public void logout(String token, String email) {
        log.info("Logout para: {}", email);

        // Registrar log
        logAtividadeService.salvarLog(
                email,
                "LOGOUT",
                "auth",
                null,
                null,
                null,
                null,
                null);

        // O token será invalidado no frontend
        // Em uma implementação mais avançada, você pode adicionar o token a uma
        // blacklist
    }

    /*
     * Verifica se o usuário logado é admin
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}