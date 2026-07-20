package com.gsz.agenda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfissionalUpdateRequest {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Email(message = "Email inválido")
    @Size(max = 191, message = "Email deve ter no máximo 191 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 500, message = "Especialidades deve ter no máximo 500 caracteres")
    private String especialidades;

    @Size(max = 255, message = "URL da foto deve ter no máximo 255 caracteres")
    private String fotoPerfil;

    private LocalTime horarioInicio;

    private LocalTime horarioFim;

    private Integer intervaloMinutos;

    private BigDecimal comissaoPercentual;

    private Boolean ativo;
}