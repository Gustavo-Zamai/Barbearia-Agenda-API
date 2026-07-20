package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.ClienteRequest;
import com.gsz.agenda.dto.request.ClienteUpdateRequest;
import com.gsz.agenda.dto.response.ClienteResponse;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.ClienteMapper;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository repository;
    private final ClienteMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final LogAtividadeService logAtividadeService;

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    /**
     * Criar um novo cliente (registro)
     */
    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        log.info("Criando novo cliente: {}", request.getEmail());

        // Validar se email já existe
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + request.getEmail());
        }

        // Validar se telefone já existe
        if (repository.existsByTelefone(request.getTelefone())) {
            throw new BusinessException("Telefone já cadastrado: " + request.getTelefone());
        }

        // Converter Request -> Entity
        Cliente cliente = mapper.toEntity(request);
        
        // Criptografar senha
        cliente.setSenhaHash(passwordEncoder.encode(request.getSenha()));

        // Salvar
        cliente = repository.save(cliente);
        
        // Registrar log
        logAtividadeService.salvarLog(
            request.getEmail(),
            "CRIAR_CLIENTE",
            "clientes",
            cliente.getId(),
            null,
            cliente,
            null,
            null
        );

        log.info("Cliente criado com sucesso: {} (ID: {})", cliente.getEmail(), cliente.getId());
        return mapper.toResponse(cliente);
    }

    /**
     * Atualizar um cliente existente (apenas o próprio usuário ou admin)
     */
    @Transactional
    public ClienteResponse atualizar(Integer id, ClienteUpdateRequest request) {
        log.info("Atualizando cliente ID: {}", id);

        // Buscar o cliente logado
        Cliente clienteLogado = getClienteLogado();
        boolean isAdmin = isAdmin();

        // Verificar se o usuário é o próprio cliente ou admin
        if (!id.equals(clienteLogado.getId()) && !isAdmin) {
            throw new BusinessException("Você não tem permissão para editar este cliente");
        }

        Cliente cliente = buscarClientePorId(id);
        
        // Guardar dados antigos para log
        Cliente dadosAntigos = Cliente.builder()
            .nome(cliente.getNome())
            .email(cliente.getEmail())
            .telefone(cliente.getTelefone())
            .dataNascimento(cliente.getDataNascimento())
            .genero(cliente.getGenero())
            .observacoes(cliente.getObservacoes())
            .ativo(cliente.getAtivo())
            .build();

        // Verificar se email já está em uso por outro cliente
        if (request.getEmail() != null && !request.getEmail().equals(cliente.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email já está em uso por outro cliente");
            }
        }

        // Verificar se telefone já está em uso por outro cliente
        if (request.getTelefone() != null && !request.getTelefone().equals(cliente.getTelefone())) {
            if (repository.existsByTelefone(request.getTelefone())) {
                throw new BusinessException("Telefone já está em uso por outro cliente");
            }
        }

        // Atualizar cliente
        mapper.updateEntity(cliente, request);

        // Salvar
        cliente = repository.save(cliente);
        
        // Registrar log
        logAtividadeService.salvarLog(
            clienteLogado.getEmail(),
            "ATUALIZAR_CLIENTE",
            "clientes",
            cliente.getId(),
            dadosAntigos,
            cliente,
            null,
            null
        );

        log.info("Cliente atualizado com sucesso: {} (ID: {})", cliente.getEmail(), cliente.getId());
        return mapper.toResponse(cliente);
    }

    /**
     * Atualizar senha do cliente
     */
    @Transactional
    public void atualizarSenha(Cliente cliente) {
        repository.save(cliente);
        log.info("Senha atualizada para cliente: {}", cliente.getEmail());
    }

    /**
     * Buscar cliente por ID (Response)
     */
    public ClienteResponse buscarPorId(Integer id) {
        Cliente cliente = buscarClientePorId(id);
        return mapper.toResponse(cliente);
    }

    /**
     * Buscar cliente por ID (Entity)
     */
    public Cliente buscarClientePorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
    }

    /**
     * Buscar cliente por email (Entity)
     */
    public Cliente buscarClientePorEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com email: " + email));
    }

    /**
     * Buscar cliente logado (através do SecurityContext)
     */
    public Cliente getClienteLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Usuário não autenticado");
        }

        String email = authentication.getName();
        return buscarClientePorEmail(email);
    }

    /**
     * Obter ID do cliente logado
     */
    public Integer getClienteLogadoId() {
        return getClienteLogado().getId();
    }

    /**
     * Verifica se o usuário logado tem role ADMIN
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

    // ============================================================
    // LISTAGENS
    // ============================================================

    /**
     * Listar todos os clientes (paginado) - Apenas admin
     */
    public Page<ClienteResponse> listarTodos(Pageable pageable) {
        Page<Cliente> clientes = repository.findAll(pageable);
        return clientes.map(mapper::toResponse);
    }

    /**
     * Listar clientes ativos
     */
    public List<ClienteResponse> listarAtivos() {
        List<Cliente> clientes = repository.findByAtivoTrue();
        return clientes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar clientes inativos
     */
    public List<ClienteResponse> listarInativos() {
        List<Cliente> clientes = repository.findByAtivoFalse();
        return clientes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar clientes por nome (contém)
     */
    public List<ClienteResponse> buscarPorNome(String nome) {
        List<Cliente> clientes = repository.findByNomeContainingIgnoreCase(nome);
        return clientes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar clientes por email (contém)
     */
    public List<ClienteResponse> buscarPorEmail(String email) {
        List<Cliente> clientes = repository.findByEmailContainingIgnoreCase(email);
        return clientes.stream().map(mapper::toResponse).toList();
    }

    // ============================================================
    // STATUS E EXCLUSÃO
    // ============================================================

    /**
     * Ativar/desativar cliente - Apenas admin
     */
    @Transactional
    public ClienteResponse alterarStatus(Integer id, Boolean ativo) {
        // Verificar se é admin
        if (!isAdmin()) {
            throw new BusinessException("Apenas administradores podem alterar o status de clientes");
        }

        Cliente cliente = buscarClientePorId(id);
        cliente.setAtivo(ativo);
        cliente = repository.save(cliente);
        
        log.info("Status do cliente {} alterado para: {}", cliente.getEmail(), ativo);
        
        // Registrar log
        logAtividadeService.salvarLog(
            getClienteLogado().getEmail(),
            "ALTERAR_STATUS_CLIENTE",
            "clientes",
            cliente.getId(),
            null,
            cliente,
            null,
            null
        );
        
        return mapper.toResponse(cliente);
    }

    /**
     * Excluir cliente (soft delete) - Apenas admin
     */
    @Transactional
    public void excluir(Integer id) {
        // Verificar se é admin
        if (!isAdmin()) {
            throw new BusinessException("Apenas administradores podem excluir clientes");
        }

        Cliente cliente = buscarClientePorId(id);
        cliente.setAtivo(false);
        repository.save(cliente);
        
        log.info("Cliente desativado: {} (ID: {})", cliente.getEmail(), cliente.getId());
        
        // Registrar log
        logAtividadeService.salvarLog(
            getClienteLogado().getEmail(),
            "EXCLUIR_CLIENTE",
            "clientes",
            cliente.getId(),
            null,
            cliente,
            null,
            null
        );
    }

    /**
     * Excluir cliente permanentemente - Apenas admin (cuidado!)
     */
    @Transactional
    public void excluirPermanentemente(Integer id) {
        // Verificar se é admin
        if (!isAdmin()) {
            throw new BusinessException("Apenas administradores podem excluir clientes permanentemente");
        }

        Cliente cliente = buscarClientePorId(id);
        repository.delete(cliente);
        
        log.info("Cliente excluído permanentemente: {} (ID: {})", cliente.getEmail(), cliente.getId());
        
        // Registrar log
        logAtividadeService.salvarLog(
            getClienteLogado().getEmail(),
            "EXCLUIR_CLIENTE_PERMANENTE",
            "clientes",
            cliente.getId(),
            null,
            cliente,
            null,
            null
        );
    }

    // ============================================================
    // VALIDAÇÕES
    // ============================================================

    /**
     * Validar credenciais do cliente
     */
    public boolean validarCredenciais(String email, String senha) {
        return repository.findByEmail(email)
                .map(cliente -> passwordEncoder.matches(senha, cliente.getSenhaHash()))
                .orElse(false);
    }

    /**
     * Verificar se email já existe
     */
    public boolean emailExiste(String email) {
        return repository.existsByEmail(email);
    }

    /**
     * Verificar se telefone já existe
     */
    public boolean telefoneExiste(String telefone) {
        return repository.existsByTelefone(telefone);
    }

    // ============================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================================

    /**
     * Buscar cliente por email (com validação de ativo)
     */
    public Cliente buscarClienteAtivoPorEmail(String email) {
        Cliente cliente = buscarClientePorEmail(email);
        if (!cliente.getAtivo()) {
            throw new BusinessException("Cliente está inativo");
        }
        return cliente;
    }
}