package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.FeriasFolgasRequest;
import com.gsz.agenda.dto.response.FeriasFolgasResponse;
import com.gsz.agenda.enums.TipoFeriasFolga;
import com.gsz.agenda.service.FeriasFolgasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ferias-folgas")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Férias e Folgas", description = "Endpoints para gerenciamento de férias e folgas")
public class FeriasFolgasController {

    private final FeriasFolgasService feriasFolgasService;

    @Operation(summary = "Solicitar férias/folga", description = "Solicita férias ou folga para um profissional")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeriasFolgasResponse> solicitar(@Valid @RequestBody FeriasFolgasRequest request) {
        FeriasFolgasResponse response = feriasFolgasService.solicitar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Aprovar solicitação", description = "Aprova uma solicitação de férias/folga (apenas admin)")
    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeriasFolgasResponse> aprovar(@PathVariable Integer id) {
        FeriasFolgasResponse response = feriasFolgasService.aprovar(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Rejeitar solicitação", description = "Rejeita uma solicitação de férias/folga (apenas admin)")
    @PatchMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> rejeitar(@PathVariable Integer id) {
        feriasFolgasService.rejeitar(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Solicitação rejeitada com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar solicitações de um profissional", description = "Lista todas as solicitações de um profissional")
    @GetMapping("/profissional/{profissionalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeriasFolgasResponse>> listarPorProfissional(@PathVariable Integer profissionalId) {
        List<FeriasFolgasResponse> response = feriasFolgasService.listarPorProfissional(profissionalId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar solicitações pendentes", description = "Lista todas as solicitações pendentes de aprovação (apenas admin)")
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeriasFolgasResponse>> listarPendentes() {
        List<FeriasFolgasResponse> response = feriasFolgasService.listarPendentes();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar solicitações por tipo", description = "Lista solicitações por tipo (apenas admin)")
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeriasFolgasResponse>> listarPorTipo(@PathVariable TipoFeriasFolga tipo) {
        List<FeriasFolgasResponse> response = feriasFolgasService.listarPorTipo(tipo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar se profissional está de férias/folga", description = "Verifica se um profissional está de férias/folga em uma data")
    @GetMapping("/profissional/{profissionalId}/verificar")
    public ResponseEntity<Boolean> isProfissionalEmFeriasFolga(
            @PathVariable Integer profissionalId,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate data) {
        boolean response = feriasFolgasService.isProfissionalEmFeriasFolga(profissionalId, data);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir solicitação", description = "Exclui uma solicitação de férias/folga (apenas admin)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        feriasFolgasService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}