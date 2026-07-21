package com.gsz.agenda.repositories;

import com.gsz.agenda.enums.StatusAgendamento;
import com.gsz.agenda.Model.Agendamento;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    // ====== BUSCAS POR ENTIDADES ======

    /**
     * Buscar agendamentos de um cliente (ordenado por data)
     */
    List<Agendamento> findByClienteOrderByDataHoraDesc(Cliente cliente);

    /**
     * Buscar agendamentos de um profissional (ordenado por data)
     */
    List<Agendamento> findByProfissionalOrderByDataHoraDesc(Profissional profissional);

    // ====== NOVO MÉTODO ADICIONADO ======
    
    /**
     * Buscar agendamentos de um profissional entre datas
     */
    List<Agendamento> findByProfissionalAndDataHoraBetweenOrderByDataHoraAsc(
        Profissional profissional,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    // ====== BUSCAS POR DATA ======

    /**
     * Buscar agendamentos entre datas
     */
    List<Agendamento> findByDataHoraBetweenOrderByDataHoraAsc(
        LocalDateTime inicio, 
        LocalDateTime fim
    );

    /**
     * Buscar agendamentos de um profissional entre datas (sem order)
     */
    List<Agendamento> findByProfissionalAndDataHoraBetween(
        Profissional profissional,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    /**
     * Buscar agendamentos de um cliente entre datas
     */
    List<Agendamento> findByClienteAndDataHoraBetweenOrderByDataHoraAsc(
        Cliente cliente,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    // ====== BUSCAS POR DATA (COM STATUS) ======

    /**
     * Buscar agendamentos de um profissional entre datas com status específico
     */
    List<Agendamento> findByProfissionalAndDataHoraBetweenAndStatusIn(
        Profissional profissional,
        LocalDateTime inicio,
        LocalDateTime fim,
        List<StatusAgendamento> status
    );

    /**
     * Buscar agendamentos do dia (hoje)
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE DATE(a.dataHora) = CURRENT_DATE " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findAgendamentosDoDia();

    /**
     * Buscar agendamentos de um profissional no dia
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND DATE(a.dataHora) = :data " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findAgendamentosDoProfissionalNoDia(
        @Param("profissional") Profissional profissional,
        @Param("data") LocalDate data
    );

    /**
     * Buscar agendamentos do dia com status específico
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE DATE(a.dataHora) = CURRENT_DATE " +
           "AND a.status = :status " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findAgendamentosDoDiaPorStatus(@Param("status") StatusAgendamento status);

    // ====== BUSCAS POR STATUS ======

    /**
     * Buscar agendamentos por status
     */
    List<Agendamento> findByStatusOrderByDataHoraDesc(StatusAgendamento status);

    /**
     * Buscar agendamentos por status (paginado)
     */
    Page<Agendamento> findByStatusOrderByDataHoraDesc(StatusAgendamento status, Pageable pageable);

    /**
     * Buscar agendamentos pendentes (para notificações)
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.status = 'PENDENTE' " +
           "AND a.dataHora BETWEEN :inicio AND :fim " +
           "AND a.lembreteEnviado = false")
    List<Agendamento> findAgendamentosPendentesParaLembrete(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // ====== CONSULTAS DE CONFLITO ======

    /**
     * Verificar se existe conflito de horário para um profissional
     */
    // Nota: Hibernate 6/7 (Spring Boot 4.1) faz checagem de tipos mais rígida no HQL.
    // DATE_ADD(...) retorna tipo desconhecido (Object), o que quebra a comparação
    // com LocalDateTime no BETWEEN. Trocado pela aritmética de data nativa do HQL:
    // (a.dataHora + (a.duracaoEstimada * 1 minute)), que preserva o tipo LocalDateTime.
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "AND ((a.dataHora BETWEEN :inicio AND :fim) " +
           "OR ((a.dataHora + (a.duracaoEstimada * 1 minute)) BETWEEN :inicio AND :fim) " +
           "OR (a.dataHora <= :inicio AND (a.dataHora + (a.duracaoEstimada * 1 minute)) >= :fim))")
    boolean existsConflitoHorario(
        @Param("profissional") Profissional profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Verificar conflito ignorando um agendamento específico (para updates)
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND a.id != :idAgendamento " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "AND ((a.dataHora BETWEEN :inicio AND :fim) " +
           "OR ((a.dataHora + (a.duracaoEstimada * 1 minute)) BETWEEN :inicio AND :fim) " +
           "OR (a.dataHora <= :inicio AND (a.dataHora + (a.duracaoEstimada * 1 minute)) >= :fim))")
    boolean existsConflitoHorarioIgnorandoId(
        @Param("profissional") Profissional profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim,
        @Param("idAgendamento") Integer idAgendamento
    );

    // ====== CONSULTAS DE FATURAMENTO ======

    /**
     * Calcular faturamento total de um profissional em um período
     */
    @Query("SELECT SUM(a.precoTotal) FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND a.status = 'CONCLUIDO' " +
           "AND a.dataHora BETWEEN :inicio AND :fim")
    BigDecimal calcularFaturamentoProfissional(
        @Param("profissional") Profissional profissional,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Calcular faturamento total (todos os profissionais) em um período
     */
    @Query("SELECT COALESCE(SUM(a.precoTotal), 0) FROM Agendamento a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "AND a.dataHora BETWEEN :inicio AND :fim")
    BigDecimal calcularFaturamentoTotal(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Contar agendamentos do dia (para métricas rápidas)
     */
    @Query("SELECT COUNT(a) FROM Agendamento a " +
           "WHERE DATE(a.dataHora) = CURRENT_DATE " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU'")
    Long countAgendamentosDoDia();

    /**
     * Calcular faturamento total por dia
     */
    @Query("SELECT DATE(a.dataHora), SUM(a.precoTotal) " +
           "FROM Agendamento a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "AND a.dataHora BETWEEN :inicio AND :fim " +
           "GROUP BY DATE(a.dataHora) " +
           "ORDER BY DATE(a.dataHora) ASC")
    List<Object[]> calcularFaturamentoPorDia(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Calcular faturamento por mês
     */
    @Query("SELECT YEAR(a.dataHora), MONTH(a.dataHora), SUM(a.precoTotal) " +
           "FROM Agendamento a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "AND a.dataHora BETWEEN :inicio AND :fim " +
           "GROUP BY YEAR(a.dataHora), MONTH(a.dataHora) " +
           "ORDER BY YEAR(a.dataHora) DESC, MONTH(a.dataHora) DESC")
    List<Object[]> calcularFaturamentoPorMes(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    // ====== CONSULTAS DE ESTATÍSTICAS ======

    /**
     * Contar agendamentos por status
     */
    @Query("SELECT a.status, COUNT(a) FROM Agendamento a " +
           "GROUP BY a.status")
    List<Object[]> countAgendamentosPorStatus();

    /**
     * Contar agendamentos de um profissional por status
     */
    @Query("SELECT a.status, COUNT(a) FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "GROUP BY a.status")
    List<Object[]> countAgendamentosPorStatusProfissional(
        @Param("profissional") Profissional profissional
    );

    /**
     * Calcular taxa de ocupação de um profissional
     */
    @Query("SELECT COUNT(a) FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND DATE(a.dataHora) = :data " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU'")
    Long countAgendamentosConfirmadosProfissionalNoDia(
        @Param("profissional") Profissional profissional,
        @Param("data") LocalDate data
    );

    /**
     * Buscar agendamentos que precisam de confirmação do cliente
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.status = 'PENDENTE' " +
           "AND a.dataHora BETWEEN :inicio AND :fim " +
           "AND a.confirmadoCliente = false")
    List<Agendamento> findAgendamentosPendentesConfirmacao(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Buscar agendamentos de hoje que já passaram (não compareceu)
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE DATE(a.dataHora) = CURRENT_DATE " +
           "AND a.dataHora < CURRENT_TIMESTAMP " +
           "AND a.status = 'CONFIRMADO'")
    List<Agendamento> findAgendamentosNaoCompareceuHoje();

    // ====== BUSCAS POR CLIENTE ======

    /**
     * Buscar agendamentos de um cliente com status específico
     */
    List<Agendamento> findByClienteAndStatusOrderByDataHoraDesc(
        Cliente cliente, 
        StatusAgendamento status
    );

    /**
     * Buscar agendamentos de um cliente entre datas
     */
    List<Agendamento> findByClienteAndDataHoraBetween(
        Cliente cliente,
        LocalDateTime inicio,
        LocalDateTime fim
    );

    /**
     * Buscar agendamentos futuros de um cliente
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.cliente = :cliente " +
           "AND a.dataHora > CURRENT_TIMESTAMP " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findAgendamentosFuturosPorCliente(@Param("cliente") Cliente cliente);

    /**
     * Buscar agendamentos passados de um cliente
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.cliente = :cliente " +
           "AND a.dataHora < CURRENT_TIMESTAMP " +
           "ORDER BY a.dataHora DESC")
    List<Agendamento> findAgendamentosPassadosPorCliente(@Param("cliente") Cliente cliente);

    // ====== BUSCAS POR PROFISSIONAL ======

    /**
     * Buscar agendamentos de um profissional com status específico
     */
    List<Agendamento> findByProfissionalAndStatusOrderByDataHoraDesc(
        Profissional profissional,
        StatusAgendamento status
    );

    /**
     * Buscar agendamentos futuros de um profissional
     */
    @Query("SELECT a FROM Agendamento a " +
           "WHERE a.profissional = :profissional " +
           "AND a.dataHora > CURRENT_TIMESTAMP " +
           "AND a.status != 'CANCELADO' " +
           "AND a.status != 'NAO_COMPARECEU' " +
           "ORDER BY a.dataHora ASC")
    List<Agendamento> findAgendamentosFuturosPorProfissional(@Param("profissional") Profissional profissional);
}