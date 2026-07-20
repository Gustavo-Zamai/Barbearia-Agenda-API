package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaturamentoResponse {

    private LocalDate data;
    private Long totalAgendamentos;
    private BigDecimal faturamentoBruto;
    private BigDecimal comissaoProfissionais;
    private BigDecimal faturamentoLiquido;
}