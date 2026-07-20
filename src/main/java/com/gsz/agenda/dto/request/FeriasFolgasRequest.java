package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.TipoFeriasFolga;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeriasFolgasRequest {

    @NotNull(message = "ID do profissional é obrigatório")
    private Integer profissionalId;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início deve ser futura")
    private LocalDate dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Future(message = "Data de fim deve ser futura")
    private LocalDate dataFim;

    @NotNull(message = "Tipo é obrigatório")
    private TipoFeriasFolga tipo;

    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    private String motivo;

    private Boolean aprovado = false;
}