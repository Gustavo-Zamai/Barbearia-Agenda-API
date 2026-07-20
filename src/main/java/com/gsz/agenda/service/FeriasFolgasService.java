package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.FeriasFolgasRequest;
import com.gsz.agenda.dto.response.FeriasFolgasResponse;
import com.gsz.agenda.enums.TipoFeriasFolga;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.FeriasFolgasMapper;
import com.gsz.agenda.Model.FeriasFolgas;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.repositories.FeriasFolgasRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeriasFolgasService {

    private final FeriasFolgasRepository repository;
    private final FeriasFolgasMapper mapper;
    private final ProfissionalService profissionalService;
    private final LogAtividadeService logAtividadeService;

    private static final int MAX_DIAS_FOLGA = 30;
    private static final int MAX_DIAS_FERIAS = 30;

    /**
     * Solicitar férias/folga
     */
    @Transactional
    public FeriasFolgasResponse solicitar(FeriasFolgasRequest request) {
        log.info("Solicitando {} para profissional ID: {}", 
            request.getTipo(), request.getProfissionalId());

        Profissional profissional = profissionalService.buscarProfissionalPorId(
            request.getProfissionalId()
        );

        // Validar datas
        validarDatas(request.getDataInicio(), request.getDataFim(), request.getTipo());

        // Verificar se já existe solicitação no período
        List<FeriasFolgas> existentes = repository.findFeriasFolgasNoPeriodo(
            profissional,
            request.getDataInicio(),
            request.getDataFim()
        );

        if (!existentes.isEmpty()) {
            throw new BusinessException("Já existe uma solicitação para este período");
        }

        // Criar solicitação
        FeriasFolgas feriasFolgas = mapper.toEntity(request);
        feriasFolgas.setProfissional(profissional);
        feriasFolgas.setAprovado(false);

        // Salvar
        feriasFolgas = repository.save(feriasFolgas);

        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "SOLICITAR_" + request.getTipo(),
            "ferias_folgas",
            feriasFolgas.getId(),
            null,
            feriasFolgas,
            null,
            null
        );

        log.info("Solicitação de {} criada com sucesso! ID: {}", 
            request.getTipo(), feriasFolgas.getId());
        
        return mapper.toResponse(feriasFolgas);
    }

    /**
     * Aprovar solicitação de férias/folga
     */
    @Transactional
    public FeriasFolgasResponse aprovar(Integer id) {
        FeriasFolgas feriasFolgas = buscarPorId(id);

        if (feriasFolgas.getAprovado()) {
            throw new BusinessException("Esta solicitação já foi aprovada");
        }

        feriasFolgas.setAprovado(true);
        feriasFolgas = repository.save(feriasFolgas);

        log.info("Solicitação ID: {} aprovada", id);
        return mapper.toResponse(feriasFolgas);
    }

    /**
     * Rejeitar solicitação de férias/folga
     */
    @Transactional
    public void rejeitar(Integer id) {
        FeriasFolgas feriasFolgas = buscarPorId(id);

        if (feriasFolgas.getAprovado()) {
            throw new BusinessException("Não é possível rejeitar uma solicitação já aprovada");
        }

        repository.delete(feriasFolgas);

        log.info("Solicitação ID: {} rejeitada e removida", id);
    }

    /**
     * Buscar solicitação por ID
     */
    public FeriasFolgas buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada com ID: " + id));
    }

    /**
     * Buscar solicitações de um profissional
     */
    public List<FeriasFolgasResponse> listarPorProfissional(Integer profissionalId) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        List<FeriasFolgas> feriasFolgas = repository.findByProfissionalOrderByDataInicioDesc(profissional);
        return feriasFolgas.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar solicitações pendentes de aprovação
     */
    public List<FeriasFolgasResponse> listarPendentes() {
        List<FeriasFolgas> feriasFolgas = repository.findByAprovadoFalseOrderByDataInicioAsc();
        return feriasFolgas.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar solicitações por tipo
     */
    public List<FeriasFolgasResponse> listarPorTipo(TipoFeriasFolga tipo) {
        List<FeriasFolgas> feriasFolgas = repository.findByTipoAndAprovadoTrueOrderByDataInicioDesc(tipo);
        return feriasFolgas.stream().map(mapper::toResponse).toList();
    }

    /**
     * Verificar se profissional está de férias/folga em uma data
     */
    public boolean isProfissionalEmFeriasFolga(Integer profissionalId, LocalDate data) {
        Profissional profissional = profissionalService.buscarProfissionalPorId(profissionalId);
        return repository.isProfissionalEmFeriasFolga(profissional, data);
    }

    /**
     * Excluir solicitação
     */
    @Transactional
    public void excluir(Integer id) {
        FeriasFolgas feriasFolgas = buscarPorId(id);
        repository.delete(feriasFolgas);
        
        log.info("Solicitação ID: {} excluída", id);
    }

    /**
     * Validar datas
     */
    private void validarDatas(LocalDate inicio, LocalDate fim, TipoFeriasFolga tipo) {
        if (inicio.isAfter(fim)) {
            throw new BusinessException("Data de início deve ser anterior à data de fim");
        }

        if (inicio.isBefore(LocalDate.now())) {
            throw new BusinessException("Data de início não pode ser no passado");
        }

        long dias = ChronoUnit.DAYS.between(inicio, fim) + 1;

        if (tipo == TipoFeriasFolga.FERIAS && dias > MAX_DIAS_FERIAS) {
            throw new BusinessException("Férias não podem exceder " + MAX_DIAS_FERIAS + " dias");
        }

        if (tipo == TipoFeriasFolga.FOLGA && dias > MAX_DIAS_FOLGA) {
            throw new BusinessException("Folga não pode exceder " + MAX_DIAS_FOLGA + " dias");
        }

        if (dias < 1) {
            throw new BusinessException("Período deve ter pelo menos 1 dia");
        }
    }
}