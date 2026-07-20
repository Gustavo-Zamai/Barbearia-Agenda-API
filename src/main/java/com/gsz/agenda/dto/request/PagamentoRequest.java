package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoRequest {

    @NotNull(message = "ID do agendamento é obrigatório")
    private Integer agendamentoId;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;

    @NotNull(message = "Método de pagamento é obrigatório")
    private MetodoPagamento metodo;

    @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
    private String observacoes;
}