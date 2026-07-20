package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.AgendamentoRequest;
import com.gsz.agenda.dto.request.AgendamentoUpdateRequest;
import com.gsz.agenda.dto.response.AgendamentoDetalhadoResponse;
import com.gsz.agenda.dto.response.AgendamentoResponse;
import com.gsz.agenda.dto.response.HorarioDisponivelResponse;
import com.gsz.agenda.enums.StatusAgendamento;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.AgendamentoMapper;
import com.gsz.agenda.Model.Agendamento;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.Model.Servico;
import com.gsz.agenda.repositories.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final AgendamentoMapper mapper;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;
    private final LogAtividadeService logAtividadeService;

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Criar um novo agendamento
     */
    @Transactional
    public AgendamentoResponse criar(AgendamentoRequest request) {
        log.info("Criando agendamento para cliente: {}, profissional: {}, data: {}", 
            request.getClienteId(), request.getProfissionalId(), request.getDataHora());

        // Buscar o cliente logado
        Cliente clienteLogado = clienteService.getClienteLogado();

        // Verificar se o cliente logado é o mesmo do agendamento
        if (!request.getClienteId().equals(clienteLogado.getId())) {
            throw new BusinessException("Você só pode criar agendamentos para si mesmo");
        }

        // Buscar entidades
        Cliente cliente = clienteService.buscarClientePorId(request.getClienteId());
        Profissional profissional = profissionalService.buscarProfissionalPorId(request.getProfissionalId());
        Servico servico = servicoService.buscarServicoPorId(request.getServicoId());

        // Validar se cliente está ativo
        if (!cliente.getAtivo()) {
            throw new BusinessException("Cliente está inativo. Não é possível fazer agendamento");
        }

        // Validar se profissional está ativo
        if (!profissional.getAtivo()) {
            throw new BusinessException("Profissional está inativo. Não é possível fazer agendamento");
        }

        // Validar se serviço está ativo
        if (!servico.getAtivo()) {
            throw new BusinessException("Serviço está inativo. Não é possível fazer agendamento");
        }

        // Validar data/hora (não pode ser passado)
        if (request.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível agendar em data/hora passada");
        }

        // Validar horário de funcionamento do profissional
        validarHorarioFuncionamento(profissional, request.getDataHora());

        // Validar conflitos de horário
        validarConflitoHorario(profissional, request.getDataHora(), servico.getDuracaoMinutos());

        // Criar agendamento
        Agendamento agendamento = mapper.toEntity(request);
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDuracaoEstimada(servico.getDuracaoMinutos());
        agendamento.setPrecoTotal(servico.getPreco());

        // Salvar
        agendamento = repository.save(agendamento);

        // Registrar log
        logAtividadeService.salvarLog(
            cliente.getEmail(),
            "CRIAR_AGENDAMENTO",
            "agendamentos",
            agendamento.getId(),
            null,
            agendamento,
            null,
            null
        );

        log.info("Agendamento criado com sucesso! ID: {}", agendamento.getId());
        return mapper.toResponse(agendamento);
    }

    /**
     * Confirmar um agendamento (pelo cliente logado)
     */
    @Transactional
    public AgendamentoResponse confirmar(Integer id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        
        // Buscar o cliente logado
        Cliente clienteLogado = clienteService.getClienteLogado();
        
        // Verificar se o cliente logado é o dono do agendamento
        if (!agendamento.getCliente().getId().equals(clienteLogado.getId())) {
            throw new BusinessException("Você só pode confirmar seus próprios agendamentos");
        }
        
        if (agendamento.getStatus() != StatusAgendamento.PENDENTE) {
            throw new BusinessException("Apenas agendamentos pendentes podem ser confirmados");
        }

        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        agendamento.setConfirmadoCliente(true);
        agendamento = repository.save(agendamento);

        log.info("Agendamento {} confirmado pelo cliente {}", id, clienteLogado.getId());
        return mapper.toResponse(agendamento);
    }

    /**
     * Cancelar um agendamento (pelo cliente logado ou admin)
     */
    @Transactional
    public void cancelar(Integer id, String motivo, String canceladoPor) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        
        // Buscar o cliente logado
        Cliente clienteLogado = clienteService.getClienteLogado();
        boolean isAdmin = clienteService.isAdmin();
        
        // Verificar se o cliente logado é o dono do agendamento ou admin
        if (!agendamento.getCliente().getId().equals(clienteLogado.getId()) && !isAdmin) {
            throw new BusinessException("Você só pode cancelar seus próprios agendamentos");
        }
        
        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO) {
            throw new BusinessException("Não é possível cancelar um agendamento já concluído");
        }

        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("Agendamento já está cancelado");
        }

        // Se o cancelamento for feito pelo admin, permitir mesmo que seja em cima da hora
        if (!isAdmin) {
            // Verificar se o cancelamento está sendo feito dentro do prazo (2h antes)
            if (LocalDateTime.now().plusHours(2).isAfter(agendamento.getDataHora())) {
                throw new BusinessException("Cancelamento só pode ser feito com 2 horas de antecedência");
            }
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(motivo);
        agendamento.setCanceladoPor(canceladoPor);
        agendamento = repository.save(agendamento);

        // Registrar log
        logAtividadeService.salvarLog(
            clienteLogado.getEmail(),
            "CANCELAR_AGENDAMENTO",
            "agendamentos",
            agendamento.getId(),
            null,
            agendamento,
            null,
            null
        );

        log.info("Agendamento {} cancelado por: {}, motivo: {}", id, canceladoPor, motivo);
    }

    /**
     * Cancelar um agendamento (admin) - método alternativo
     */
    @Transactional
    public void cancelarPorAdmin(Integer id, String motivo) {
        cancelar(id, motivo, "ADMIN");
    }

    /**
     * Concluir um agendamento (apenas admin)
     */
    @Transactional
    public AgendamentoResponse concluir(Integer id) {
        // Verificar se é admin
        if (!clienteService.isAdmin()) {
            throw new BusinessException("Apenas administradores podem concluir agendamentos");
        }

        Agendamento agendamento = buscarAgendamentoPorId(id);
        
        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO && 
            agendamento.getStatus() != StatusAgendamento.EM_ANDAMENTO) {
            throw new BusinessException("Apenas agendamentos confirmados ou em andamento podem ser concluídos");
        }

        agendamento.setStatus(StatusAgendamento.CONCLUIDO);
        agendamento = repository.save(agendamento);

        log.info("Agendamento {} concluído", id);
        return mapper.toResponse(agendamento);
    }

    /**
     * Iniciar agendamento (marcar como em andamento)
     */
    @Transactional
    public AgendamentoResponse iniciar(Integer id) {
        // Verificar se é admin
        if (!clienteService.isAdmin()) {
            throw new BusinessException("Apenas administradores podem iniciar agendamentos");
        }

        Agendamento agendamento = buscarAgendamentoPorId(id);
        
        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser iniciados");
        }

        agendamento.setStatus(StatusAgendamento.EM_ANDAMENTO);
        agendamento = repository.save(agendamento);

        log.info("Agendamento {} iniciado", id);
        return mapper.toResponse(agendamento);
    }

    /**
     * Marcar como não compareceu
     */
    @Transactional
    public AgendamentoResponse marcarNaoCompareceu(Integer id) {
        // Verificar se é admin
        if (!clienteService.isAdmin()) {
            throw new BusinessException("Apenas administradores podem marcar como não compareceu");
        }

        Agendamento agendamento = buscarAgendamentoPorId(id);
        
        if (agendamento.getStatus() != StatusAgendamento.CONFIRMADO) {
            throw new BusinessException("Apenas agendamentos confirmados podem ser marcados como não compareceu");
        }

        agendamento.setStatus(StatusAgendamento.NAO_COMPARECEU);
        agendamento = repository.save(agendamento);

        log.info("Agendamento {} marcado como não compareceu", id);
        return mapper.toResponse(agendamento);
    }

    /**
     * Buscar agendamento por ID
     */
    public Agendamento buscarAgendamentoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
    }

    /**
     * Buscar agendamento por ID (Response)
     */
    public AgendamentoResponse buscarPorId(Integer id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        return mapper.toResponse(agendamento);
    }

    /**
     * Buscar agendamento por ID (Detalhado)
     */
    public AgendamentoDetalhadoResponse buscarDetalhadoPorId(Integer id) {
        Agendamento agendamento = buscarAgendamentoPorId(id);
        return mapper.toDetalhedResponse(agendamento);
    }

    /**
     * Listar agendamentos do cliente logado
     */
    public List<AgendamentoResponse> listarPorClienteLogado() {
        Cliente cliente = clienteService.getClienteLogado();
        List<Agendamento> agendamentos = repository.findByClienteOrderByDataHoraDesc(cliente);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar agendamentos de um cliente
     */
    public List<AgendamentoResponse> listarPorCliente(Integer clienteId) {
        // Verificar se é admin ou o próprio cliente
        Cliente clienteLogado = clienteService.getClienteLogado();
        if (!clienteId.equals(clienteLogado.getId()) && !clienteService.isAdmin()) {
            throw new BusinessException("Você só pode ver seus próprios agendamentos");
        }

        Cliente cliente = clienteService.buscarClientePorId(clienteId);
        List<Agendamento> agendamentos = repository.findByClienteOrderByDataHoraDesc(cliente);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar agendamentos de um profissional
     */
    public List<AgendamentoResponse> listarPorProfissional(Integer profissionalId) {
        // Verificar se é admin
        if (!clienteService.isAdmin()) {
            throw new BusinessException("Apenas administradores podem ver agendamentos de profissionais");
        }

        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        List<Agendamento> agendamentos = repository.findByProfissionalOrderByDataHoraDesc(profissional);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar agendamentos de um profissional em uma data
     */
    public List<AgendamentoResponse> listarPorProfissionalData(Integer profissionalId, LocalDate data) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(LocalTime.MAX);
        
        List<Agendamento> agendamentos = repository.findByProfissionalAndDataHoraBetween(
            profissional, inicio, fim
        );
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar agendamentos futuros do cliente logado
     */
    public List<AgendamentoResponse> listarFuturosPorClienteLogado() {
        Cliente cliente = clienteService.getClienteLogado();
        List<Agendamento> agendamentos = repository.findAgendamentosFuturosPorCliente(cliente);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Listar agendamentos passados do cliente logado
     */
    public List<AgendamentoResponse> listarPassadosPorClienteLogado() {
        Cliente cliente = clienteService.getClienteLogado();
        List<Agendamento> agendamentos = repository.findAgendamentosPassadosPorCliente(cliente);
        return agendamentos.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar horários disponíveis para um profissional
     */
    public List<HorarioDisponivelResponse> buscarHorariosDisponiveis(
            Integer profissionalId, LocalDate data, Integer duracao) {
        
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        
        // Horário de início e fim do profissional
        LocalTime inicio = profissional.getHorarioInicio();
        LocalTime fim = profissional.getHorarioFim();
        
        // Buscar agendamentos do dia
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);
        
        List<Agendamento> agendamentos = repository.findByProfissionalAndDataHoraBetween(
            profissional, inicioDia, fimDia
        );
        
        List<HorarioDisponivelResponse> horariosDisponiveis = new ArrayList<>();
        
        // Gerar slots de horário (a cada 15 minutos)
        LocalTime horarioAtual = inicio;
        while (horarioAtual.isBefore(fim) || horarioAtual.equals(fim)) {
            LocalDateTime dataHora = data.atTime(horarioAtual);
            LocalDateTime fimSlot = dataHora.plusMinutes(duracao);
            
            // Verificar se o slot está disponível
            boolean disponivel = true;
            for (Agendamento agendamento : agendamentos) {
                LocalDateTime inicioAgendamento = agendamento.getDataHora();
                LocalDateTime fimAgendamento = inicioAgendamento
                    .plusMinutes(agendamento.getDuracaoEstimada());
                
                if ((dataHora.isBefore(fimAgendamento) && dataHora.isAfter(inicioAgendamento)) ||
                    (fimSlot.isBefore(fimAgendamento) && fimSlot.isAfter(inicioAgendamento)) ||
                    (dataHora.isBefore(inicioAgendamento) && fimSlot.isAfter(fimAgendamento)) ||
                    dataHora.equals(inicioAgendamento)) {
                    disponivel = false;
                    break;
                }
            }
            
            horariosDisponiveis.add(HorarioDisponivelResponse.builder()
                .horario(dataHora)
                .horarioFormatado(dataHora.format(HORA_FORMATTER))
                .disponivel(disponivel)
                .profissionalId(profissional.getId())
                .profissionalNome(profissional.getNome())
                .build());
            
            // Avançar para próximo slot (15 minutos)
            horarioAtual = horarioAtual.plusMinutes(15);
        }
        
        return horariosDisponiveis;
    }

    /**
     * Validar horário de funcionamento do profissional
     */
    private void validarHorarioFuncionamento(Profissional profissional, LocalDateTime dataHora) {
        LocalTime hora = dataHora.toLocalTime();
        
        if (hora.isBefore(profissional.getHorarioInicio()) || 
            hora.isAfter(profissional.getHorarioFim())) {
            throw new BusinessException("Horário fora do expediente do profissional");
        }
    }

    /**
     * Validar conflitos de horário
     */
    private void validarConflitoHorario(Profissional profissional, LocalDateTime dataHora, Integer duracao) {
        LocalDateTime fim = dataHora.plusMinutes(duracao);
        
        boolean conflito = repository.existsConflitoHorario(
            profissional,
            dataHora,
            fim
        );
        
        if (conflito) {
            throw new BusinessException("Horário indisponível. Escolha outro horário");
        }
    }
}