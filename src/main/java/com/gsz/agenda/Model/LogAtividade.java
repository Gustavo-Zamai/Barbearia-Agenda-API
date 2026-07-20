package com.gsz.agenda.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_atividades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder  // <-- Certifique-se que está aqui
public class LogAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String usuario;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(length = 50)
    private String tabela;

    @Column(name = "registro_id")
    private Integer registroId;

    @Lob
    @Column(columnDefinition = "BLOB")
    private String dadosAnteriores;

    @Lob
    @Column(columnDefinition = "BLOB")
    private String dadosNovos;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}