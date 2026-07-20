package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.ServicoRequest;
import com.gsz.agenda.dto.request.ServicoUpdateRequest;
import com.gsz.agenda.dto.response.ServicoResponse;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.ServicoMapper;
import com.gsz.agenda.Model.Servico;
import com.gsz.agenda.repositories.ServicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoService {

    private final ServicoRepository repository;
    private final ServicoMapper mapper;
    private final LogAtividadeService logAtividadeService;

    /**
     * Criar um novo serviço
     */
    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        log.info("Criando novo serviço: {}", request.getNome());

        // Validar nome único
        if (repository.existsByNomeIgnoreCase(request.getNome())) {
            throw new BusinessException("Serviço com nome '" + request.getNome() + "' já existe");
        }

        // Validar duração
        if (request.getDuracaoMinutos() < 5) {
            throw new BusinessException("Duração mínima do serviço é de 5 minutos");
        }

        // Validar preço
        if (request.getPreco().doubleValue() <= 0) {
            throw new BusinessException("Preço deve ser maior que zero");
        }

        // Converter Request -> Entity
        Servico servico = mapper.toEntity(request);

        // Salvar
        servico = repository.save(servico);
        
        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "CRIAR_SERVICO",
            "servicos",
            servico.getId(),
            null,
            servico,
            null,
            null
        );

        log.info("Serviço criado: {} (ID: {})", servico.getNome(), servico.getId());
        return mapper.toResponse(servico);
    }

    /**
     * Atualizar um serviço
     */
    @Transactional
    public ServicoResponse atualizar(Integer id, ServicoUpdateRequest request) {
        log.info("Atualizando serviço ID: {}", id);

        Servico servico = buscarServicoPorId(id);
        
        // Guardar dados antigos
        Servico dadosAntigos = Servico.builder()
            .nome(servico.getNome())
            .descricao(servico.getDescricao())
            .categoria(servico.getCategoria())
            .duracaoMinutos(servico.getDuracaoMinutos())
            .preco(servico.getPreco())
            .precoPromocional(servico.getPrecoPromocional())
            .ativo(servico.getAtivo())
            .build();

        // Validar nome único
        if (request.getNome() != null && !request.getNome().equalsIgnoreCase(servico.getNome())) {
            if (repository.existsByNomeIgnoreCase(request.getNome())) {
                throw new BusinessException("Serviço com nome '" + request.getNome() + "' já existe");
            }
        }

        // Validar duração
        if (request.getDuracaoMinutos() != null && request.getDuracaoMinutos() < 5) {
            throw new BusinessException("Duração mínima do serviço é de 5 minutos");
        }

        // Validar preço
        if (request.getPreco() != null && request.getPreco().doubleValue() <= 0) {
            throw new BusinessException("Preço deve ser maior que zero");
        }

        // Atualizar
        mapper.updateEntity(servico, request);
        servico = repository.save(servico);
        
        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "ATUALIZAR_SERVICO",
            "servicos",
            servico.getId(),
            dadosAntigos,
            servico,
            null,
            null
        );

        log.info("Serviço atualizado: {} (ID: {})", servico.getNome(), servico.getId());
        return mapper.toResponse(servico);
    }

    /**
     * Buscar serviço por ID
     */
    public Servico buscarServicoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com ID: " + id));
    }

    /**
     * Buscar serviço por ID (Response)
     */
    public ServicoResponse buscarPorId(Integer id) {
        Servico servico = buscarServicoPorId(id);
        return mapper.toResponse(servico);
    }

    /**
     * Listar todos os serviços (paginado)
     */
    public Page<ServicoResponse> listarTodos(Pageable pageable) {
        Page<Servico> servicos = repository.findAll(pageable);
        return servicos.map(mapper::toResponse);
    }

    /**
     * Listar serviços ativos
     */
    public List<ServicoResponse> listarAtivos() {
        List<Servico> servicos = repository.findByAtivoTrue();
        return servicos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar serviços por categoria
     */
    public List<ServicoResponse> buscarPorCategoria(String categoria) {
        List<Servico> servicos = repository.findByCategoriaIgnoreCaseAndAtivoTrue(categoria);
        return servicos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar serviços por nome
     */
    public List<ServicoResponse> buscarPorNome(String nome) {
        List<Servico> servicos = repository.findByNomeContainingIgnoreCase(nome);
        return servicos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Ativar/desativar serviço
     */
    @Transactional
    public ServicoResponse alterarStatus(Integer id, Boolean ativo) {
        Servico servico = buscarServicoPorId(id);
        servico.setAtivo(ativo);
        servico = repository.save(servico);
        
        log.info("Status do serviço {} alterado para: {}", servico.getNome(), ativo);
        return mapper.toResponse(servico);
    }
}