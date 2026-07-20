package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.HorarioFuncionamentoRequest;
import com.gsz.agenda.dto.response.HorarioFuncionamentoResponse;
import com.gsz.agenda.enums.DiaSemana;
import com.gsz.agenda.service.HorarioFuncionamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios-funcionamento")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Horários de Funcionamento", description = "Endpoints para gerenciamento de horários de funcionamento")
public class HorarioFuncionamentoController {

    private final HorarioFuncionamentoService horarioFuncionamentoService;

    @Operation(summary = "Criar horário", description = "Cria um novo horário de funcionamento (apenas admin)")
    @PostMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioFuncionamentoResponse> criar(
            @PathVariable Integer profissionalId,
            @Valid @RequestBody HorarioFuncionamentoRequest request) {
        HorarioFuncionamentoResponse response = horarioFuncionamentoService.criar(profissionalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Atualizar horário", description = "Atualiza um horário de funcionamento (apenas admin)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioFuncionamentoResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody HorarioFuncionamentoRequest request) {
        HorarioFuncionamentoResponse response = horarioFuncionamentoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar horários de um profissional", description = "Lista todos os horários de um profissional")
    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<HorarioFuncionamentoResponse>> listarPorProfissional(@PathVariable Integer profissionalId) {
        List<HorarioFuncionamentoResponse> response = horarioFuncionamentoService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar horário por profissional e dia", description = "Busca um horário específico")
    @GetMapping("/profissional/{profissionalId}/dia/{diaSemana}")
    public ResponseEntity<HorarioFuncionamentoResponse> buscarPorProfissionalDia(
            @PathVariable Integer profissionalId,
            @PathVariable DiaSemana diaSemana) {
        HorarioFuncionamentoResponse response = horarioFuncionamentoService.buscarPorProfissionalDia(
            profissionalId, diaSemana
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar se profissional trabalha no dia", description = "Verifica se um profissional trabalha em um determinado dia")
    @GetMapping("/profissional/{profissionalId}/trabalha/{diaSemana}")
    public ResponseEntity<Boolean> isProfissionalTrabalhaNoDia(
            @PathVariable Integer profissionalId,
            @PathVariable DiaSemana diaSemana) {
        boolean response = horarioFuncionamentoService.isProfissionalTrabalhaNoDia(profissionalId, diaSemana);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ativar/desativar horário", description = "Altera o status do horário (apenas admin)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioFuncionamentoResponse> alterarStatus(
            @PathVariable Integer id,
            @RequestParam Boolean ativo) {
        HorarioFuncionamentoResponse response = horarioFuncionamentoService.alterarStatus(id, ativo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir horário", description = "Exclui um horário de funcionamento (apenas admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        horarioFuncionamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}