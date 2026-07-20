package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.AgendamentoRequest;
import com.gsz.agenda.dto.response.AgendamentoDetalhadoResponse;
import com.gsz.agenda.dto.response.AgendamentoResponse;
import com.gsz.agenda.dto.response.HorarioDisponivelResponse;
import com.gsz.agenda.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Agendamentos", description = "Endpoints para gerenciamento de agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento para o cliente logado")
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
        AgendamentoResponse response = agendamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Confirmar agendamento", description = "Confirma um agendamento pendente")
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<AgendamentoResponse> confirmar(@PathVariable Integer id) {
        AgendamentoResponse response = agendamentoService.confirmar(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar agendamento", description = "Cancela um agendamento")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Map<String, String>> cancelar(
            @PathVariable Integer id,
            @RequestParam String motivo,
            @RequestParam(defaultValue = "CLIENTE") String canceladoPor) {
        
        agendamentoService.cancelar(id, motivo, canceladoPor);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Agendamento cancelado com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Iniciar agendamento", description = "Marca agendamento como em andamento (apenas admin)")
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamentoResponse> iniciar(@PathVariable Integer id) {
        AgendamentoResponse response = agendamentoService.iniciar(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Concluir agendamento", description = "Conclui um agendamento (apenas admin)")
    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamentoResponse> concluir(@PathVariable Integer id) {
        AgendamentoResponse response = agendamentoService.concluir(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Marcar como não compareceu", description = "Marca agendamento como não compareceu (apenas admin)")
    @PatchMapping("/{id}/nao-compareceu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgendamentoResponse> marcarNaoCompareceu(@PathVariable Integer id) {
        AgendamentoResponse response = agendamentoService.marcarNaoCompareceu(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar agendamento por ID", description = "Busca um agendamento pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDetalhadoResponse> buscarPorId(@PathVariable Integer id) {
        AgendamentoDetalhadoResponse response = agendamentoService.buscarDetalhadoPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos do cliente logado", description = "Lista todos os agendamentos do cliente autenticado")
    @GetMapping("/cliente")
    public ResponseEntity<List<AgendamentoResponse>> listarPorClienteLogado() {
        List<AgendamentoResponse> response = agendamentoService.listarPorClienteLogado();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos futuros do cliente logado", description = "Lista agendamentos futuros do cliente autenticado")
    @GetMapping("/cliente/futuros")
    public ResponseEntity<List<AgendamentoResponse>> listarFuturosPorClienteLogado() {
        List<AgendamentoResponse> response = agendamentoService.listarFuturosPorClienteLogado();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos passados do cliente logado", description = "Lista agendamentos passados do cliente autenticado")
    @GetMapping("/cliente/passados")
    public ResponseEntity<List<AgendamentoResponse>> listarPassadosPorClienteLogado() {
        List<AgendamentoResponse> response = agendamentoService.listarPassadosPorClienteLogado();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos de um cliente", description = "Lista agendamentos de um cliente específico")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoResponse>> listarPorCliente(@PathVariable Integer clienteId) {
        List<AgendamentoResponse> response = agendamentoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos de um profissional", description = "Lista todos os agendamentos de um profissional (apenas admin)")
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AgendamentoResponse>> listarPorProfissional(@PathVariable Integer profissionalId) {
        List<AgendamentoResponse> response = agendamentoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agendamentos de um profissional em uma data", description = "Lista agendamentos de um profissional em uma data específica")
    @GetMapping("/profissional/{profissionalId}/data")
    public ResponseEntity<List<AgendamentoResponse>> listarPorProfissionalData(
            @PathVariable Integer profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<AgendamentoResponse> response = agendamentoService.listarPorProfissionalData(profissionalId, data);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar horários disponíveis", description = "Busca horários disponíveis para um profissional em uma data")
    @GetMapping("/disponibilidade")
    public ResponseEntity<List<HorarioDisponivelResponse>> buscarHorariosDisponiveis(
            @RequestParam Integer profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(defaultValue = "30") Integer duracao) {
        List<HorarioDisponivelResponse> response = agendamentoService.buscarHorariosDisponiveis(
            profissionalId, data, duracao
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os agendamentos", description = "Lista todos os agendamentos com paginação (apenas admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AgendamentoResponse>> listarTodos(
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        // TODO: Implementar método no service
        return ResponseEntity.ok(null);
    }
}