package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // Métricas principais
    private Long agendamentosHoje;
    private Long agendamentosSemana;
    private Long agendamentosMes;
    private BigDecimal faturamentoHoje;
    private BigDecimal faturamentoSemana;
    private BigDecimal faturamentoMes;
    private BigDecimal faturamentoAno;
    private Long clientesAtivos;
    private Long profissionaisAtivos;
    private BigDecimal taxaOcupacao;

    // Listas
    private List<AgendamentoResponse> agendamentosDoDia;
    private List<AgendamentoResponse> proximosAgendamentos;
    private List<FaturamentoResponse> faturamentoPorDia;
    private List<RankingProfissionalResponse> rankingProfissionais;
    private List<ServicoMaisAgendadoResponse> servicosMaisAgendados;
}