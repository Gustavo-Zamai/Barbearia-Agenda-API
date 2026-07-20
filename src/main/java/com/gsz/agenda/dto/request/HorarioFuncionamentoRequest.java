package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioFuncionamentoRequest {

    @NotNull(message = "Dia da semana é obrigatório")
    private DiaSemana diaSemana;

    @NotNull(message = "Horário de início é obrigatório")
    private LocalTime horarioInicio;

    @NotNull(message = "Horário de fim é obrigatório")
    private LocalTime horarioFim;

    private Integer intervaloMinutos = 15;

    private Boolean ativo = true;
}