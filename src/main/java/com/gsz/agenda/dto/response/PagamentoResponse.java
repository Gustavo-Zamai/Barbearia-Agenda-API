package com.gsz.agenda.dto.response;

import com.gsz.agenda.enums.MetodoPagamento;
import com.gsz.agenda.enums.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoResponse {

    private Integer id;
    private Integer agendamentoId;
    private BigDecimal valor;
    private MetodoPagamento metodo;
    private StatusPagamento status;
    private LocalDateTime dataPagamento;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}