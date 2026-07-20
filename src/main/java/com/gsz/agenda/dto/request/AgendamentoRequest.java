package com.gsz.agenda.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoRequest {

    @NotNull(message = "ID do cliente é obrigatório")
    private Integer clienteId;

    @NotNull(message = "ID do profissional é obrigatório")
    private Integer profissionalId;

    @NotNull(message = "ID do serviço é obrigatório")
    private Integer servicoId;

    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "Data e hora devem ser futuras")
    private LocalDateTime dataHora;

    private String observacoes;
}