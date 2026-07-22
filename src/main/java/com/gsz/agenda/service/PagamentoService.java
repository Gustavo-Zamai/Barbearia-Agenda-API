package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.PagamentoRequest;
import com.gsz.agenda.dto.response.PagamentoResponse;
import com.gsz.agenda.enums.StatusPagamento;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.PagamentoMapper;
import com.gsz.agenda.Model.Agendamento;
import com.gsz.agenda.Model.Pagamento;
import com.gsz.agenda.repositories.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagamentoService {

    private final PagamentoRepository repository;
    private final PagamentoMapper mapper;
    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final LogAtividadeService logAtividadeService;

    /**
     * Registrar um novo pagamento
     */
    @Transactional
    public PagamentoResponse registrar(PagamentoRequest request) {
        log.info("Registrando pagamento para agendamento ID: {}", request.getAgendamentoId());

        // Buscar agendamento
        Agendamento agendamento = agendamentoService.buscarAgendamentoPorId(
            request.getAgendamentoId()
        );

        // Verificar se já existe pagamento para este agendamento
        if (repository.findByAgendamentoId(request.getAgendamentoId()).isPresent()) {
            throw new BusinessException("Este agendamento já possui um pagamento registrado");
        }

        // Cliente só pode registrar pagamento do próprio agendamento (admin pode de qualquer um)
        if (!clienteService.isAdmin()) {
            Integer clienteLogadoId = clienteService.getClienteLogadoId();
            if (!clienteLogadoId.equals(agendamento.getCliente().getId())) {
                throw new BusinessException("Você não tem permissão para registrar pagamento deste agendamento");
            }
        }

        // Validar se o valor do pagamento é igual ao valor do agendamento
        if (request.getValor().compareTo(agendamento.getPrecoTotal()) != 0) {
            throw new BusinessException(
                "Valor do pagamento deve ser igual ao valor do agendamento: R$ " + 
                agendamento.getPrecoTotal()
            );
        }

        // Criar pagamento
        Pagamento pagamento = mapper.toEntity(request);
        pagamento.setAgendamento(agendamento);
        pagamento.setStatus(StatusPagamento.PENDENTE);

        // Salvar
        pagamento = repository.save(pagamento);

        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "REGISTRAR_PAGAMENTO",
            "pagamentos",
            pagamento.getId(),
            null,
            pagamento,
            null,
            null
        );

        log.info("Pagamento registrado com sucesso! ID: {}", pagamento.getId());
        return mapper.toResponse(pagamento);
    }

    /**
     * Confirmar pagamento (pago)
     */
    @Transactional
    public PagamentoResponse confirmarPagamento(Integer id) {
        Pagamento pagamento = buscarPagamentoPorId(id);

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new BusinessException("Apenas pagamentos pendentes podem ser confirmados");
        }

        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento = repository.save(pagamento);

        log.info("Pagamento ID: {} confirmado", id);
        return mapper.toResponse(pagamento);
    }

    /**
     * Cancelar pagamento
     */
    @Transactional
    public void cancelarPagamento(Integer id, String motivo) {
        Pagamento pagamento = buscarPagamentoPorId(id);

        if (pagamento.getStatus() == StatusPagamento.PAGO) {
            throw new BusinessException("Não é possível cancelar um pagamento já confirmado");
        }

        pagamento.setStatus(StatusPagamento.CANCELADO);
        pagamento.setObservacoes("Cancelado: " + motivo);
        pagamento = repository.save(pagamento);

        log.info("Pagamento ID: {} cancelado. Motivo: {}", id, motivo);
    }

    /**
     * Reembolsar pagamento
     */
    @Transactional
    public PagamentoResponse reembolsar(Integer id) {
        Pagamento pagamento = buscarPagamentoPorId(id);

        if (pagamento.getStatus() != StatusPagamento.PAGO) {
            throw new BusinessException("Apenas pagamentos confirmados podem ser reembolsados");
        }

        pagamento.setStatus(StatusPagamento.REEMBOLSADO);
        pagamento = repository.save(pagamento);

        log.info("Pagamento ID: {} reembolsado", id);
        return mapper.toResponse(pagamento);
    }

    /**
     * Buscar pagamento por ID
     */
    public Pagamento buscarPagamentoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado com ID: " + id));
    }

    /**
     * Buscar pagamento por ID (Response)
     */
    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorId(Integer id) {
        Pagamento pagamento = buscarPagamentoPorId(id);
        verificarPermissaoAcesso(pagamento);
        return mapper.toResponse(pagamento);
    }

    /**
     * Buscar pagamento por agendamento
     */
    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorAgendamento(Integer agendamentoId) {
        Pagamento pagamento = repository.findByAgendamentoId(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Pagamento não encontrado para agendamento ID: " + agendamentoId
                ));
        verificarPermissaoAcesso(pagamento);
        return mapper.toResponse(pagamento);
    }

    /**
     * Listar pagamentos por status
     */
    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorStatus(StatusPagamento status) {
        List<Pagamento> pagamentos = repository.findByStatusOrderByCreatedAtDesc(status);
        return pagamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar pagamentos por método
     */
    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorMetodo(String metodo) {
        List<Pagamento> pagamentos = repository.findByMetodoOrderByCreatedAtDesc(
            com.gsz.agenda.enums.MetodoPagamento.valueOf(metodo)
        );
        return pagamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar pagamentos por período
     */
    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Pagamento> pagamentos = repository.findByCreatedAtBetweenOrderByCreatedAtDesc(inicio, fim);
        return pagamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Calcular total de pagamentos no período
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalNoPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal total = repository.calcularTotalPagamentosNoPeriodo(inicio, fim);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Listar pagamentos pendentes
     */
    @Transactional(readOnly = true)
    public List<PagamentoResponse> listarPendentes() {
        List<Pagamento> pagamentos = repository.findPagamentosPendentes(
            LocalDateTime.now().minusDays(7)
        );
        return pagamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Garante que o cliente logado só acesse pagamentos dos próprios
     * agendamentos — admin (profissional) acessa qualquer um.
     */
    private void verificarPermissaoAcesso(Pagamento pagamento) {
        if (clienteService.isAdmin()) {
            return;
        }
        Integer clienteLogadoId = clienteService.getClienteLogadoId();
        Integer donoAgendamentoId = pagamento.getAgendamento().getCliente().getId();
        if (!clienteLogadoId.equals(donoAgendamentoId)) {
            throw new BusinessException("Você não tem permissão para acessar este pagamento");
        }
    }
}