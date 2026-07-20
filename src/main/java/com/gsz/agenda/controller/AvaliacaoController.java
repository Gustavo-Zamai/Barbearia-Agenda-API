package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.AvaliacaoRequest;
import com.gsz.agenda.dto.response.AvaliacaoResponse;
import com.gsz.agenda.service.AvaliacaoService;
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
@RequestMapping("/api/avaliacoes")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Avaliações", description = "Endpoints para gerenciamento de avaliações")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Operation(summary = "Criar avaliação", description = "Cria uma nova avaliação para um agendamento concluído")
    @PostMapping
    public ResponseEntity<AvaliacaoResponse> criar(@Valid @RequestBody AvaliacaoRequest request) {
        AvaliacaoResponse response = avaliacaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Buscar avaliação por ID", description = "Busca uma avaliação pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> buscarPorId(@PathVariable Integer id) {
        AvaliacaoResponse response = avaliacaoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar avaliação por agendamento", description = "Busca uma avaliação pelo ID do agendamento")
    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<AvaliacaoResponse> buscarPorAgendamento(@PathVariable Integer agendamentoId) {
        AvaliacaoResponse response = avaliacaoService.buscarPorAgendamento(agendamentoId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar avaliações de um profissional", description = "Lista todas as avaliações de um profissional")
    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<AvaliacaoResponse>> listarPorProfissional(@PathVariable Integer profissionalId) {
        List<AvaliacaoResponse> response = avaliacaoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar avaliações de um cliente", description = "Lista todas as avaliações de um cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AvaliacaoResponse>> listarPorCliente(@PathVariable Integer clienteId) {
        List<AvaliacaoResponse> response = avaliacaoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todas as avaliações", description = "Lista todas as avaliações com paginação (apenas admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AvaliacaoResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AvaliacaoResponse> response = avaliacaoService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar últimas avaliações", description = "Lista as últimas avaliações")
    @GetMapping("/ultimas")
    public ResponseEntity<List<AvaliacaoResponse>> listarUltimas(@RequestParam(defaultValue = "10") Integer limite) {
        List<AvaliacaoResponse> response = avaliacaoService.listarUltimas(limite);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular média de avaliações", description = "Calcula a média de avaliações de um profissional")
    @GetMapping("/media/{profissionalId}")
    public ResponseEntity<Double> calcularMedia(@PathVariable Integer profissionalId) {
        Double media = avaliacaoService.calcularMediaProfissional(profissionalId);
        return ResponseEntity.ok(media);
    }

    @Operation(summary = "Excluir avaliação", description = "Exclui uma avaliação (apenas admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        avaliacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}