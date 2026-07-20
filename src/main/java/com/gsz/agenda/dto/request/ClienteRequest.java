package com.gsz.agenda.dto.request;

import com.gsz.agenda.enums.Genero;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class ClienteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 191, message = "Email deve ter no máximo 191 caracteres")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres")
    private String senha;

    private LocalDate dataNascimento;

    private Genero genero;

    private String observacoes;
}