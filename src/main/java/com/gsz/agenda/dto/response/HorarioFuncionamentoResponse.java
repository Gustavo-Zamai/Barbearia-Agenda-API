package com.gsz.agenda.dto.response;

import com.gsz.agenda.enums.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioFuncionamentoResponse {

    private Integer id;
    private Integer profissionalId;
    private String profissionalNome;
    private DiaSemana diaSemana;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private Integer intervaloMinutos;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}