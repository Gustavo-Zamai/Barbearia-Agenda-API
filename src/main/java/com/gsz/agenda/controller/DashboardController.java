package com.gsz.agenda.controller;

import com.gsz.agenda.dto.response.*;
import com.gsz.agenda.service.AgendamentoService;
import com.gsz.agenda.service.ClienteService;
import com.gsz.agenda.service.ProfissionalService;
import com.gsz.agenda.service.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Endpoints para dados do dashboard (apenas admin)")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    @Operation(summary = "Dados do dashboard", description = "Retorna dados consolidados do dashboard")
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        // TODO: Implementar método no service
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Agendamentos do dia", description = "Retorna os agendamentos do dia")
    @GetMapping("/agendamentos-hoje")
    public ResponseEntity<List<AgendamentoResponse>> getAgendamentosHoje() {
        // TODO: Implementar método no service
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Faturamento por período", description = "Retorna o faturamento em um período")
    @GetMapping("/faturamento")
    public ResponseEntity<Map<String, BigDecimal>> getFaturamento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        
        Map<String, BigDecimal> response = new HashMap<>();
        // TODO: Implementar método no service
        response.put("total", BigDecimal.ZERO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Ranking de profissionais", description = "Retorna o ranking de profissionais")
    @GetMapping("/ranking-profissionais")
    public ResponseEntity<List<RankingProfissionalResponse>> getRankingProfissionais() {
        // TODO: Implementar método no service
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Serviços mais agendados", description = "Retorna os serviços mais agendados")
    @GetMapping("/servicos-mais-agendados")
    public ResponseEntity<List<ServicoMaisAgendadoResponse>> getServicosMaisAgendados() {
        // TODO: Implementar método no service
        return ResponseEntity.ok(null);
    }

    @Operation(summary = "Métricas rápidas", description = "Retorna métricas rápidas do dashboard")
    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> getMetricas() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("totalClientes", clienteService.listarAtivos().size());
        response.put("totalProfissionais", profissionalService.listarAtivos().size());
        response.put("totalServicos", servicoService.listarAtivos().size());
        response.put("totalAgendamentosHoje", 0); // TODO: Implementar
        
        return ResponseEntity.ok(response);
    }
}