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
public class AgendamentoDetalhadoResponse {

    private Integer id;
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

    // Dados do Cliente
    private Integer clienteId;
    private String clienteNome;
    private String clienteEmail;
    private String clienteTelefone;

    // Dados do Profissional
    private Integer profissionalId;
    private String profissionalNome;
    private String profissionalEmail;
    private String profissionalTelefone;
    private String profissionalEspecialidades;

    // Dados do Serviço
    private Integer servicoId;
    private String servicoNome;
    private String servicoDescricao;
    private Integer servicoDuracao;

    // Dados do Pagamento (se existir)
    private PagamentoResponse pagamento;

    // Dados da Avaliação (se existir)
    private AvaliacaoResponse avaliacao;
}