package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDisponivelResponse {

    private LocalDateTime horario;
    private String horarioFormatado;
    private Boolean disponivel;
    private Integer profissionalId;
    private String profissionalNome;
}