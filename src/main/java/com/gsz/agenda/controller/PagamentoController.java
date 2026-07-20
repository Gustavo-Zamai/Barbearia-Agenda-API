package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.PagamentoRequest;
import com.gsz.agenda.dto.response.PagamentoResponse;
import com.gsz.agenda.enums.StatusPagamento;
import com.gsz.agenda.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Pagamentos", description = "Endpoints para gerenciamento de pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @Operation(summary = "Registrar pagamento", description = "Registra um novo pagamento para um agendamento")
    @PostMapping
    public ResponseEntity<PagamentoResponse> registrar(@Valid @RequestBody PagamentoRequest request) {
        PagamentoResponse response = pagamentoService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Confirmar pagamento", description = "Confirma um pagamento pendente (apenas admin)")
    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoResponse> confirmar(@PathVariable Integer id) {
        PagamentoResponse response = pagamentoService.confirmarPagamento(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancelar pagamento", description = "Cancela um pagamento pendente (apenas admin)")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cancelar(
            @PathVariable Integer id,
            @RequestParam String motivo) {
        pagamentoService.cancelarPagamento(id, motivo);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pagamento cancelado com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reembolsar pagamento", description = "Reembolsa um pagamento confirmado (apenas admin)")
    @PatchMapping("/{id}/reembolsar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagamentoResponse> reembolsar(@PathVariable Integer id) {
        PagamentoResponse response = pagamentoService.reembolsar(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar pagamento por ID", description = "Busca um pagamento pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponse> buscarPorId(@PathVariable Integer id) {
        PagamentoResponse response = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar pagamento por agendamento", description = "Busca um pagamento pelo ID do agendamento")
    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<PagamentoResponse> buscarPorAgendamento(@PathVariable Integer agendamentoId) {
        PagamentoResponse response = pagamentoService.buscarPorAgendamento(agendamentoId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar pagamentos por status", description = "Lista pagamentos por status (apenas admin)")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagamentoResponse>> listarPorStatus(@PathVariable StatusPagamento status) {
        List<PagamentoResponse> response = pagamentoService.listarPorStatus(status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular total no período", description = "Calcula o total de pagamentos em um período (apenas admin)")
    @GetMapping("/total-periodo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> calcularTotalNoPeriodo(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        BigDecimal total = pagamentoService.calcularTotalNoPeriodo(inicio, fim);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Listar pagamentos pendentes", description = "Lista pagamentos pendentes (apenas admin)")
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagamentoResponse>> listarPendentes() {
        List<PagamentoResponse> response = pagamentoService.listarPendentes();
        return ResponseEntity.ok(response);
    }
}