package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.ClienteRequest;
import com.gsz.agenda.dto.request.ClienteUpdateRequest;
import com.gsz.agenda.dto.response.ClienteResponse;
import com.gsz.agenda.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Criar cliente", description = "Registra um novo cliente")
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza um cliente existente")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        ClienteResponse response = clienteService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar cliente por ID", description = "Busca um cliente pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Integer id) {
        ClienteResponse response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar cliente logado", description = "Busca os dados do cliente autenticado")
    @GetMapping("/me")
    public ResponseEntity<ClienteResponse> buscarClienteLogado() {
        ClienteResponse response = clienteService.buscarPorId(clienteService.getClienteLogadoId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os clientes", description = "Lista todos os clientes com paginação (apenas admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClienteResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ClienteResponse> response = clienteService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar clientes ativos", description = "Lista todos os clientes ativos")
    @GetMapping("/ativos")
    public ResponseEntity<List<ClienteResponse>> listarAtivos() {
        List<ClienteResponse> response = clienteService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar clientes por nome", description = "Busca clientes pelo nome")
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscarPorNome(@RequestParam String nome) {
        List<ClienteResponse> response = clienteService.buscarPorNome(nome);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ativar/desativar cliente", description = "Altera o status do cliente (apenas admin)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> alterarStatus(
            @PathVariable Integer id,
            @RequestParam Boolean ativo) {
        ClienteResponse response = clienteService.alterarStatus(id, ativo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir cliente", description = "Desativa um cliente (apenas admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}