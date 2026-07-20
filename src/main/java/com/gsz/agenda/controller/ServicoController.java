package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.ServicoRequest;
import com.gsz.agenda.dto.request.ServicoUpdateRequest;
import com.gsz.agenda.dto.response.ServicoResponse;
import com.gsz.agenda.service.ServicoService;
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
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Serviços", description = "Endpoints para gerenciamento de serviços")
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Criar serviço", description = "Cadastra um novo serviço (apenas admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoRequest request) {
        ServicoResponse response = servicoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar serviço", description = "Atualiza um serviço existente (apenas admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ServicoUpdateRequest request) {
        ServicoResponse response = servicoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar serviço por ID", description = "Busca um serviço pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponse> buscarPorId(@PathVariable Integer id) {
        ServicoResponse response = servicoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os serviços", description = "Lista todos os serviços com paginação")
    @GetMapping
    public ResponseEntity<Page<ServicoResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ServicoResponse> response = servicoService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar serviços ativos", description = "Lista todos os serviços ativos")
    @GetMapping("/ativos")
    public ResponseEntity<List<ServicoResponse>> listarAtivos() {
        List<ServicoResponse> response = servicoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar serviços por categoria", description = "Busca serviços por categoria")
    @GetMapping("/categoria")
    public ResponseEntity<List<ServicoResponse>> buscarPorCategoria(@RequestParam String categoria) {
        List<ServicoResponse> response = servicoService.buscarPorCategoria(categoria);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar serviços por nome", description = "Busca serviços pelo nome")
    @GetMapping("/buscar")
    public ResponseEntity<List<ServicoResponse>> buscarPorNome(@RequestParam String nome) {
        List<ServicoResponse> response = servicoService.buscarPorNome(nome);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ativar/desativar serviço", description = "Altera o status do serviço (apenas admin)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoResponse> alterarStatus(
            @PathVariable Integer id,
            @RequestParam Boolean ativo) {
        ServicoResponse response = servicoService.alterarStatus(id, ativo);
        return ResponseEntity.ok(response);
    }
}