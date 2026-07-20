package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoResponse {

    private Integer id;
    private String nome;
    private String descricao;
    private String categoria;
    private Integer duracaoMinutos;
    private BigDecimal preco;
    private BigDecimal precoPromocional;
    private BigDecimal comissaoPercentual;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}