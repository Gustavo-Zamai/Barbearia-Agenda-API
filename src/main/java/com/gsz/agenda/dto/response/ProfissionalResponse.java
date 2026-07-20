package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfissionalResponse {

    private Integer id;
    private String nome;
    private String email;
    private String telefone;
    private String especialidades;
    private String fotoPerfil;
    private LocalTime horarioInicio;
    private LocalTime horarioFim;
    private Integer intervaloMinutos;
    private BigDecimal comissaoPercentual;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}