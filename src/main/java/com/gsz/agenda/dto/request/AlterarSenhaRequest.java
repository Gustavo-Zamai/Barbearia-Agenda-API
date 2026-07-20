package com.gsz.agenda.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlterarSenhaRequest {

    @NotBlank(message = "Senha atual é obrigatória")
    private String senhaAtual;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, max = 255, message = "Nova senha deve ter entre 6 e 255 caracteres")
    private String novaSenha;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmacaoSenha;
}