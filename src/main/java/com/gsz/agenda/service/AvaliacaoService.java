package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.AvaliacaoRequest;
import com.gsz.agenda.dto.response.AvaliacaoResponse;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.AvaliacaoMapper;
import com.gsz.agenda.Model.Agendamento;
import com.gsz.agenda.Model.Avaliacao;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.repositories.AvaliacaoRepository;
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
public class AvaliacaoService {

    private final AvaliacaoRepository repository;
    private final AvaliacaoMapper mapper;
    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final LogAtividadeService logAtividadeService;

    /**
     * Criar uma nova avaliação
     */
    @Transactional
    public AvaliacaoResponse criar(AvaliacaoRequest request) {
        log.info("Criando avaliação para agendamento ID: {}", request.getAgendamentoId());

        // Verificar se agendamento existe
        Agendamento agendamento = agendamentoService.buscarAgendamentoPorId(
            request.getAgendamentoId()
        );

        // Verificar se o agendamento já foi concluído
        if (!agendamento.getStatus().name().equals("CONCLUIDO")) {
            throw new BusinessException("Apenas agendamentos concluídos podem ser avaliados");
        }

        // Verificar se já existe avaliação para este agendamento
        if (repository.existsByAgendamentoId(request.getAgendamentoId())) {
            throw new BusinessException("Este agendamento já foi avaliado");
        }

        // Buscar cliente e profissional
        Cliente cliente = agendamento.getCliente();
        Profissional profissional = agendamento.getProfissional();

        // Criar avaliação
        Avaliacao avaliacao = mapper.toEntity(request);
        avaliacao.setAgendamento(agendamento);
        avaliacao.setCliente(cliente);
        avaliacao.setProfissional(profissional);

        // Salvar
        avaliacao = repository.save(avaliacao);

        // Registrar log
        logAtividadeService.salvarLog(
            cliente.getEmail(),
            "CRIAR_AVALIACAO",
            "avaliacoes",
            avaliacao.getId(),
            null,
            avaliacao,
            null,
            null
        );

        log.info("Avaliação criada com sucesso! ID: {}, Nota: {}", 
            avaliacao.getId(), avaliacao.getNota());
        
        return mapper.toResponse(avaliacao);
    }

    /**
     * Buscar avaliação por ID
     */
    public AvaliacaoResponse buscarPorId(Integer id) {
        Avaliacao avaliacao = buscarAvaliacaoPorId(id);
        return mapper.toResponse(avaliacao);
    }

    /**
     * Buscar avaliação por ID (entidade)
     */
    public Avaliacao buscarAvaliacaoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada com ID: " + id));
    }

    /**
     * Buscar avaliação por agendamento
     */
    public AvaliacaoResponse buscarPorAgendamento(Integer agendamentoId) {
        Avaliacao avaliacao = repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Avaliação não encontrada para agendamento ID: " + agendamentoId
                ));
        return mapper.toResponse(avaliacao);
    }

    /**
     * Listar avaliações de um profissional
     */
    public List<AvaliacaoResponse> listarPorProfissional(Integer profissionalId) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        List<Avaliacao> avaliacoes = repository.findByProfissionalOrderByCreatedAtDesc(profissional);
        return avaliacoes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar avaliações de um cliente
     */
    public List<AvaliacaoResponse> listarPorCliente(Integer clienteId) {
        Cliente cliente = clienteService.buscarClientePorId(clienteId);
        List<Avaliacao> avaliacoes = repository.findByClienteOrderByCreatedAtDesc(cliente);
        return avaliacoes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar todas as avaliações (paginado)
     */
    public Page<AvaliacaoResponse> listarTodos(Pageable pageable) {
        Page<Avaliacao> avaliacoes = repository.findAll(pageable);
        return avaliacoes.map(mapper::toResponse);
    }

    /**
     * Calcular média de avaliações de um profissional
     */
    public Double calcularMediaProfissional(Integer profissionalId) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        Double media = repository.calcularMediaAvaliacoesProfissional(profissional);
        return media != null ? media : 0.0;
    }

    /**
     * Listar avaliações com nota mínima
     */
    public List<AvaliacaoResponse> listarPorProfissionalNotaMinima(
            Integer profissionalId, Integer notaMinima) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        List<Avaliacao> avaliacoes = repository.findByProfissionalAndNotaGreaterThanEqual(
            profissional, notaMinima
        );
        return avaliacoes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar últimas avaliações
     */
    public List<AvaliacaoResponse> listarUltimas(Integer limite) {
        List<Avaliacao> avaliacoes = repository.findUltimasAvaliacoes(
            org.springframework.data.domain.PageRequest.of(0, limite)
        );
        return avaliacoes.stream().map(mapper::toResponse).toList();
    }

    /**
     * Excluir avaliação (apenas admin)
     */
    @Transactional
    public void excluir(Integer id) {
        Avaliacao avaliacao = buscarAvaliacaoPorId(id);
        repository.delete(avaliacao);
        
        log.info("Avaliação ID: {} excluída", id);
    }
}