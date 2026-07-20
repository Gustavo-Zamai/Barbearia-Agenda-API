package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoMaisAgendadoResponse {

    private Integer profissionalId;
    private String profissionalNome;
    private Long totalAgendamentos;
    private BigDecimal faturamentoTotal;
    private Double notaMedia;
    private Long totalAvaliacoes;
}