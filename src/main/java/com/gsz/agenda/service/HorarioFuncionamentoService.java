package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.HorarioFuncionamentoRequest;
import com.gsz.agenda.dto.response.HorarioFuncionamentoResponse;
import com.gsz.agenda.enums.DiaSemana;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.HorarioFuncionamentoMapper;
import com.gsz.agenda.Model.HorarioFuncionamento;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.repositories.HorarioFuncionamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioFuncionamentoService {

    private final HorarioFuncionamentoRepository repository;
    private final HorarioFuncionamentoMapper mapper;
    private final ProfissionalService profissionalService;
    private final LogAtividadeService logAtividadeService;

    /**
     * Criar horário de funcionamento para um profissional
     */
    @Transactional
    public HorarioFuncionamentoResponse criar(
            Integer profissionalId, 
            HorarioFuncionamentoRequest request) {
        
        log.info("Criando horário para profissional ID: {}, dia: {}", 
            profissionalId, request.getDiaSemana());

        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);

        // Validar se já existe horário para este dia
        if (repository.findByProfissionalAndDiaSemana(profissional, request.getDiaSemana()).isPresent()) {
            throw new BusinessException("Já existe horário cadastrado para este dia");
        }

        // Validar horários
        validarHorarios(request.getHorarioInicio(), request.getHorarioFim());

        // Criar horário
        HorarioFuncionamento horario = mapper.toEntity(request);
        horario.setProfissional(profissional);

        // Salvar
        horario = repository.save(horario);

        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "CRIAR_HORARIO_FUNCIONAMENTO",
            "horarios_funcionamento",
            horario.getId(),
            null,
            horario,
            null,
            null
        );

        log.info("Horário criado com sucesso! ID: {}", horario.getId());
        return mapper.toResponse(horario);
    }

    /**
     * Atualizar horário de funcionamento
     */
    @Transactional
    public HorarioFuncionamentoResponse atualizar(Integer id, HorarioFuncionamentoRequest request) {
        log.info("Atualizando horário ID: {}", id);

        HorarioFuncionamento horario = buscarHorarioPorId(id);

        // Validar horários
        validarHorarios(request.getHorarioInicio(), request.getHorarioFim());

        // Atualizar
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHorarioInicio(request.getHorarioInicio());
        horario.setHorarioFim(request.getHorarioFim());
        horario.setIntervaloMinutos(request.getIntervaloMinutos());
        horario.setAtivo(request.getAtivo());

        horario = repository.save(horario);

        log.info("Horário ID: {} atualizado", id);
        return mapper.toResponse(horario);
    }

    /**
     * Buscar horário por ID
     */
    public HorarioFuncionamento buscarHorarioPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horário não encontrado com ID: " + id));
    }

    /**
     * Buscar horários de um profissional
     */
    public List<HorarioFuncionamentoResponse> listarPorProfissional(Integer profissionalId) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        List<HorarioFuncionamento> horarios = repository.findByProfissionalAndAtivoTrueOrderByDiaSemana(
            profissional
        );
        return horarios.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar horário de um profissional em um dia específico
     */
    public HorarioFuncionamentoResponse buscarPorProfissionalDia(
            Integer profissionalId, DiaSemana diaSemana) {
        
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        HorarioFuncionamento horario = repository.findByProfissionalAndDiaSemana(profissional, diaSemana)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Horário não encontrado para profissional ID: " + profissionalId + 
                    " no dia: " + diaSemana
                ));
        return mapper.toResponse(horario);
    }

    /**
     * Verificar se profissional trabalha em um dia
     */
    public boolean isProfissionalTrabalhaNoDia(Integer profissionalId, DiaSemana diaSemana) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        return repository.isProfissionalTrabalhaNoDia(profissional, diaSemana);
    }

    /**
     * Ativar/desativar horário
     */
    @Transactional
    public HorarioFuncionamentoResponse alterarStatus(Integer id, Boolean ativo) {
        HorarioFuncionamento horario = buscarHorarioPorId(id);
        horario.setAtivo(ativo);
        horario = repository.save(horario);
        
        log.info("Status do horário ID: {} alterado para: {}", id, ativo);
        return mapper.toResponse(horario);
    }

    /**
     * Excluir horário
     */
    @Transactional
    public void excluir(Integer id) {
        HorarioFuncionamento horario = buscarHorarioPorId(id);
        repository.delete(horario);
        
        log.info("Horário ID: {} excluído", id);
    }

    /**
     * Validar horários
     */
    private void validarHorarios(LocalTime inicio, LocalTime fim) {
        if (inicio.isAfter(fim) || inicio.equals(fim)) {
            throw new BusinessException("Horário de início deve ser anterior ao horário de fim");
        }

        if (inicio.isBefore(LocalTime.of(6, 0))) {
            throw new BusinessException("Horário de início não pode ser antes das 06:00");
        }

        if (fim.isAfter(LocalTime.of(23, 59))) {
            throw new BusinessException("Horário de fim não pode ser após as 23:59");
        }
    }
}