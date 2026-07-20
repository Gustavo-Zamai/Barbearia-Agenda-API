package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.ProfissionalRequest;
import com.gsz.agenda.dto.request.ProfissionalUpdateRequest;
import com.gsz.agenda.dto.response.ProfissionalResponse;
import com.gsz.agenda.service.ProfissionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Profissionais", description = "Endpoints para gerenciamento de profissionais")
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @Operation(summary = "Criar profissional", description = "Cadastra um novo profissional (apenas admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfissionalResponse> criar(@Valid @RequestBody ProfissionalRequest request) {
        ProfissionalResponse response = profissionalService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar profissional", description = "Atualiza um profissional existente (apenas admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfissionalResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProfissionalUpdateRequest request) {
        ProfissionalResponse response = profissionalService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar profissional por ID", description = "Busca um profissional pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(@PathVariable Integer id) {
        ProfissionalResponse response = profissionalService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os profissionais", description = "Lista todos os profissionais com paginação")
    @GetMapping
    public ResponseEntity<Page<ProfissionalResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProfissionalResponse> response = profissionalService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar profissionais ativos", description = "Lista todos os profissionais ativos")
    @GetMapping("/ativos")
    public ResponseEntity<List<ProfissionalResponse>> listarAtivos() {
        List<ProfissionalResponse> response = profissionalService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar profissionais por especialidade", description = "Busca profissionais por especialidade")
    @GetMapping("/especialidade")
    public ResponseEntity<List<ProfissionalResponse>> buscarPorEspecialidade(@RequestParam String especialidade) {
        List<ProfissionalResponse> response = profissionalService.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ativar/desativar profissional", description = "Altera o status do profissional (apenas admin)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfissionalResponse> alterarStatus(
            @PathVariable Integer id,
            @RequestParam Boolean ativo) {
        ProfissionalResponse response = profissionalService.alterarStatus(id, ativo);
        return ResponseEntity.ok(response);
    }
}