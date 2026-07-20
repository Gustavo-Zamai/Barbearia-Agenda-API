package com.gsz.agenda.repositories;

import com.gsz.agenda.Model.LogAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogAtividadeRepository extends JpaRepository<LogAtividade, Integer> {

    /**
     * Buscar logs por usuário
     */
    List<LogAtividade> findByUsuarioOrderByCreatedAtDesc(String usuario);

    /**
     * Buscar logs por ação
     */
    List<LogAtividade> findByAcaoOrderByCreatedAtDesc(String acao);

    /**
     * Buscar logs por tabela e registro
     */
    List<LogAtividade> findByTabelaAndRegistroIdOrderByCreatedAtDesc(
        String tabela,
        Integer registroId
    );

    /**
     * Buscar logs por período
     */
    List<LogAtividade> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime inicio,
        LocalDateTime fim
    );

    /**
     * Buscar últimas ações de um usuário
     */
    @Query("SELECT l FROM LogAtividade l " +
           "WHERE l.usuario = :usuario " +
           "ORDER BY l.createdAt DESC")
    List<LogAtividade> findUltimasAcoesUsuario(
        @Param("usuario") String usuario,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Buscar logs de erro
     */
    @Query("SELECT l FROM LogAtividade l " +
           "WHERE l.acao LIKE 'ERRO_%' " +
           "ORDER BY l.createdAt DESC")
    List<LogAtividade> findLogsErro();

    /**
     * Contar ações por tipo
     */
    @Query("SELECT l.acao, COUNT(l) FROM LogAtividade l " +
           "WHERE l.createdAt BETWEEN :inicio AND :fim " +
           "GROUP BY l.acao")
    List<Object[]> countAcoesPorPeriodo(
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    /**
     * Buscar logs por IP
     */
    List<LogAtividade> findByIpOrderByCreatedAtDesc(String ip);
}