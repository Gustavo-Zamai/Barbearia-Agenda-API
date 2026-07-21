package com.gsz.agenda.service;

import com.gsz.agenda.dto.request.ProfissionalRequest;
import com.gsz.agenda.dto.request.ProfissionalUpdateRequest;
import com.gsz.agenda.dto.response.ProfissionalResponse;
import com.gsz.agenda.exception.BusinessException;
import com.gsz.agenda.exception.ResourceNotFoundException;
import com.gsz.agenda.mapper.ProfissionalMapper;
import com.gsz.agenda.Model.Profissional;
import com.gsz.agenda.repositories.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final ProfissionalMapper mapper;
    private final LogAtividadeService logAtividadeService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Criar um novo profissional
     */
    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        log.info("Criando novo profissional: {}", request.getEmail());

        // Validar email único
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + request.getEmail());
        }

        // Validar horários
        validarHorarios(request.getHorarioInicio(), request.getHorarioFim());

        // Converter Request -> Entity
        Profissional profissional = mapper.toEntity(request);

        // Definir senha (hash) — não é mapeada automaticamente pois o
        // campo de origem é "senha" (texto puro) e o destino é "senhaHash"
        profissional.setSenhaHash(passwordEncoder.encode(request.getSenha()));

        // Salvar
        profissional = repository.save(profissional);
        
        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "CRIAR_PROFISSIONAL",
            "profissionais",
            profissional.getId(),
            null,
            profissional,
            null,
            null
        );

        log.info("Profissional criado com sucesso: {} (ID: {})", profissional.getEmail(), profissional.getId());
        return mapper.toResponse(profissional);
    }

    /**
     * Atualizar um profissional
     */
    @Transactional
    public ProfissionalResponse atualizar(Integer id, ProfissionalUpdateRequest request) {
        log.info("Atualizando profissional ID: {}", id);

        Profissional profissional = buscarProfissionalPorId(id);
        
        // Guardar dados antigos
        Profissional dadosAntigos = Profissional.builder()
            .nome(profissional.getNome())
            .email(profissional.getEmail())
            .telefone(profissional.getTelefone())
            .especialidades(profissional.getEspecialidades())
            .horarioInicio(profissional.getHorarioInicio())
            .horarioFim(profissional.getHorarioFim())
            .comissaoPercentual(profissional.getComissaoPercentual())
            .ativo(profissional.getAtivo())
            .build();

        // Validar email
        if (request.getEmail() != null && !request.getEmail().equals(profissional.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email já está em uso por outro profissional");
            }
        }

        // Validar horários
        if (request.getHorarioInicio() != null && request.getHorarioFim() != null) {
            validarHorarios(request.getHorarioInicio(), request.getHorarioFim());
        }

        // Atualizar
        mapper.updateEntity(profissional, request);
        profissional = repository.save(profissional);
        
        // Registrar log
        logAtividadeService.salvarLog(
            "SISTEMA",
            "ATUALIZAR_PROFISSIONAL",
            "profissionais",
            profissional.getId(),
            dadosAntigos,
            profissional,
            null,
            null
        );

        log.info("Profissional atualizado: {} (ID: {})", profissional.getEmail(), profissional.getId());
        return mapper.toResponse(profissional);
    }

    /**
     * Buscar profissional por ID
     */
    public Profissional buscarProfissionalPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + id));
    }

    /**
     * Buscar profissional por ID (Response)
     */
    public ProfissionalResponse buscarPorId(Integer id) {
        Profissional profissional = buscarProfissionalPorId(id);
        return mapper.toResponse(profissional);
    }

    /**
     * Listar todos os profissionais (paginado)
     */
    public Page<ProfissionalResponse> listarTodos(Pageable pageable) {
        Page<Profissional> profissionais = repository.findAll(pageable);
        return profissionais.map(mapper::toResponse);
    }

    /**
     * Listar profissionais ativos
     */
    public List<ProfissionalResponse> listarAtivos() {
        List<Profissional> profissionais = repository.findByAtivoTrue();
        return profissionais.stream().map(mapper::toResponse).toList();
    }

    /**
     * Buscar profissionais por especialidade
     */
    public List<ProfissionalResponse> buscarPorEspecialidade(String especialidade) {
        List<Profissional> profissionais = repository.findByEspecialidadesContainingIgnoreCase(especialidade);
        return profissionais.stream().map(mapper::toResponse).toList();
    }

    /**
     * Ativar/desativar profissional
     */
    @Transactional
    public ProfissionalResponse alterarStatus(Integer id, Boolean ativo) {
        Profissional profissional = buscarProfissionalPorId(id);
        profissional.setAtivo(ativo);
        profissional = repository.save(profissional);
        
        log.info("Status do profissional {} alterado para: {}", profissional.getNome(), ativo);
        return mapper.toResponse(profissional);
    }

    /**
     * Validar horários de funcionamento
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