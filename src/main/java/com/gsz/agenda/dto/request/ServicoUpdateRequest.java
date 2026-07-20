package com.gsz.agenda.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicoUpdateRequest {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    private String categoria;

    @Positive(message = "Duração deve ser maior que zero")
    private Integer duracaoMinutos;

    @Positive(message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    @Positive(message = "Preço promocional deve ser maior que zero")
    private BigDecimal precoPromocional;

    @Positive(message = "Comissão percentual deve ser maior que zero")
    private BigDecimal comissaoPercentual;

    private Boolean ativo;
}