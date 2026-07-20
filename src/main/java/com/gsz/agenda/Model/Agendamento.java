package com.gsz.agenda.Model;

import com.gsz.agenda.enums.StatusAgendamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "duracao_estimada", nullable = false)
    private Integer duracaoEstimada;

    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "lembrete_enviado")
    private Boolean lembreteEnviado = false;

    @Column(name = "confirmado_cliente")
    private Boolean confirmadoCliente = false;

    @Column(name = "cancelado_por", length = 20)
    private String canceladoPor;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Pagamento pagamento;

    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Avaliacao avaliacao;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}