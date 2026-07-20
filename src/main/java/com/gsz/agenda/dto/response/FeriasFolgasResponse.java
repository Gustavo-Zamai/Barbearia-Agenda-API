package com.gsz.agenda.dto.response;

import com.gsz.agenda.enums.TipoFeriasFolga;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeriasFolgasResponse {

    private Integer id;
    private Integer profissionalId;
    private String profissionalNome;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private TipoFeriasFolga tipo;
    private String motivo;
    private Boolean aprovado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}