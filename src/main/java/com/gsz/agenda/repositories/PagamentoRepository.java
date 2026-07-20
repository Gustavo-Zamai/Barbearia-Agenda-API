package com.gsz.agenda.repositories;

import com.gsz.agenda.enums.MetodoPagamento;
import com.gsz.agenda.enums.StatusPagamento;
import com.gsz.agenda.Model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    /**
     * Buscar pagamento por agendamento
     */
    Optional<Pagamento> findByAgendamentoId(Integer agendamentoId);

    /**
     * Buscar pagamentos por status
     */
    List<Pagamento> findByStatusOrderByCreatedAtDesc(StatusPagamento status);

    /**
     * Buscar pagamentos por método
     */
    List<Pagamento> findByMetodoOrderByCreatedAtDesc(MetodoPagamento metodo);

    /**
     * Buscar pagamentos por período
     */
    List<Pagamento> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime inicio,
        LocalDateTime fim
    );

    /**
     * Buscar pagamentos pendentes
     */
    @Query("SELECT p FROM Pagamento p " +
           "WHERE p.status = 'PENDENTE' " +
           "AND p.createdAt < :dataLimite")
    List<Pagamento> findPagamentosPendentes(@Param("dataLimite") LocalDateTime dataLimite);

    /**
     * Calcular total de pagamentos por período
     */
    @Query("SELECT SUM(p.valor) FROM Pagamento p " +
           "WHERE p.status = 'PAGO' " +
           "AND p.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal calcularTotalPagamentosNoPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Calcular total por método de pagamento
     */
    @Query("SELECT p.metodo, SUM(p.valor) FROM Pagamento p " +
           "WHERE p.status = 'PAGO' " +
           "AND p.dataPagamento BETWEEN :inicio AND :fim " +
           "GROUP BY p.metodo")
    List<Object[]> calcularTotalPorMetodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}