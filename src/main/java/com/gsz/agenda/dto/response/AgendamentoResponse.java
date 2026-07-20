package com.gsz.agenda.dto.response;

import com.gsz.agenda.enums.StatusAgendamento;
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
public class AgendamentoResponse {

    private Integer id;
    private Integer clienteId;
    private String clienteNome;
    private Integer profissionalId;
    private String profissionalNome;
    private Integer servicoId;
    private String servicoNome;
    private LocalDateTime dataHora;
    private Integer duracaoEstimada;
    private BigDecimal precoTotal;
    private StatusAgendamento status;
    private String observacoes;
    private Boolean lembreteEnviado;
    private Boolean confirmadoCliente;
    private String canceladoPor;
    private String motivoCancelamento;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}