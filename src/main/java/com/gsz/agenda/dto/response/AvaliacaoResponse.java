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
public class AvaliacaoResponse {

    private Integer id;
    private Integer agendamentoId;
    private Integer clienteId;
    private String clienteNome;
    private Integer profissionalId;
    private String profissionalNome;
    private Integer nota;
    private String comentario;
    private LocalDateTime createdAt;
}