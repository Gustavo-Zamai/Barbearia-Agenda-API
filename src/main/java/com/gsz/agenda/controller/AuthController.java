package com.gsz.agenda.controller;

import com.gsz.agenda.dto.request.AlterarSenhaRequest;
import com.gsz.agenda.dto.request.LoginRequest;
import com.gsz.agenda.dto.response.LoginResponse;
import com.gsz.agenda.service.AuthService;
import com.gsz.agenda.dto.request.RegistroClienteRequest;
import com.gsz.agenda.dto.request.RegistroProfissionalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "*")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de sessão")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Autenticar usuário", description = "Realiza login e retorna token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PutMapping("/alterar-senha")
    public ResponseEntity<Map<String, String>> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        authService.alterarSenha(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Senha alterada com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Realiza logout do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        // O token será invalidado no frontend
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validar token", description = "Verifica se o token JWT é válido")
    @GetMapping("/validar-token")
    public ResponseEntity<Map<String, Boolean>> validarToken() {
        // Se chegou aqui, o token é válido (graças ao filtro JWT)
        Map<String, Boolean> response = new HashMap<>();
        response.put("valido", true);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cadastrar cliente", description = "Cria uma nova conta de cliente e retorna token JWT")
    @PostMapping("/register/cliente")
    public ResponseEntity<LoginResponse> registrarCliente(@Valid @RequestBody RegistroClienteRequest request) {
        LoginResponse response = authService.cadastrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cadastrar profissional", description = "Cria uma nova conta de profissional e retorna token JWT")
    @PostMapping("/register/profissional")
    public ResponseEntity<LoginResponse> registrarProfissional(
            @Valid @RequestBody RegistroProfissionalRequest request) {
        LoginResponse response = authService.cadastrarProfissional(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}