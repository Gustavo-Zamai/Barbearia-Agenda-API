package com.gsz.agenda.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private Long expiracao;
    private Integer usuarioId;
    private String nome;
    private String email;
    private String role;
}