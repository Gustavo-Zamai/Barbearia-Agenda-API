package com.gsz.agenda.dto.response;

import com.gsz.agenda.enums.Genero;
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
public class ClienteResponse {

    private Integer id;
    private String nome;
    private String email;
    private String telefone;
    private LocalDate dataNascimento;
    private Genero genero;
    private String observacoes;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}