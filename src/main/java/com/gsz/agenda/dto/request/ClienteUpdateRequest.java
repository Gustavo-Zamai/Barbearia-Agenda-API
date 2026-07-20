package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.Genero;
import jakarta.validation.constraints.Email;
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
public class ClienteUpdateRequest {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Email(message = "Email inválido")
    @Size(max = 191, message = "Email deve ter no máximo 191 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    private LocalDate dataNascimento;

    private Genero genero;

    @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
    private String observacoes;

    private Boolean ativo;
}