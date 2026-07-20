package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.StatusAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoUpdateRequest {

    private Integer profissionalId;

    private Integer servicoId;

    @Future(message = "Data e hora devem ser futuras")
    private LocalDateTime dataHora;

    @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
    private String observacoes;

    private StatusAgendamento status;

    private Boolean confirmadoCliente;
}