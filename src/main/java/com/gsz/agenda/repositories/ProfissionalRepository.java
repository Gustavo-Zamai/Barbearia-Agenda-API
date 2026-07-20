package com.gsz.agenda.repositories;

import com.gsz.agenda.Model.Profissional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Integer> {

    // ====== BUSCAS BÁSICAS ======

    /**
     * Buscar profissional por email
     */
    Optional<Profissional> findByEmail(String email);

    /**
     * Buscar profissional por email (ignorando maiúsculas/minúsculas)
     */
    Optional<Profissional> findByEmailIgnoreCase(String email);

    /**
     * Verificar se existe profissional com este email
     */
    boolean existsByEmail(String email);

    /**
     * Verificar se existe profissional com este email (ignorando ID)
     */
    @Query("SELECT COUNT(p) > 0 FROM Profissional p WHERE p.email = :email AND p.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Integer id);

    // ====== LISTAGENS ======

    /**
     * Listar todos os profissionais ativos
     */
    List<Profissional> findByAtivoTrue();

    /**
     * Listar todos os profissionais inativos
     */
    List<Profissional> findByAtivoFalse();

    /**
     * Listar profissionais ativos com paginação
     */
    Page<Profissional> findByAtivoTrue(Pageable pageable);

    /**
     * Buscar profissionais por nome (contém)
     */
    List<Profissional> findByNomeContainingIgnoreCase(String nome);

    /**
     * Buscar profissionais por especialidade
     */
    List<Profissional> findByEspecialidadesContainingIgnoreCase(String especialidade);

    /**
     * Buscar profissionais por especialidade (paginado)
     */
    Page<Profissional> findByEspecialidadesContainingIgnoreCase(String especialidade, Pageable pageable);

    // ====== CONSULTAS PERSONALIZADAS ======

    /**
     * Buscar profissionais disponíveis em um horário específico, em uma data específica.
     *
     * Correções em relação à versão anterior (Hibernate 6/7, Spring Boot 4.1):
     * 1) DATE_ADD(...) foi trocado por (a.dataHora + (a.duracaoEstimada * 1 minute)),
     *    que preserva o tipo LocalDateTime (DATE_ADD retornava tipo desconhecido/Object).
     * 2) A subquery de conflito agora compara SEMPRE LocalDateTime com LocalDateTime
     *    (inicioJanela/fimJanela), em vez de misturar a.dataHora (LocalDateTime) com
     *    um parâmetro LocalTime cru — essa mistura de tipos também não é mais aceita
     *    pela checagem estrita do Hibernate 6/7, e antes ignorava silenciosamente a data
     *    (comparava só a hora, em qualquer dia do histórico).
     *
     * IMPORTANTE: quem chama este método precisa montar inicioJanela/fimJanela
     * combinando a data desejada com a hora, por exemplo:
     *   LocalDateTime inicioJanela = LocalDateTime.of(data, hora);
     *   LocalDateTime fimJanela   = LocalDateTime.of(data, horaFim);
     */
    @Query("SELECT p FROM Profissional p " +
           "WHERE p.ativo = true " +
           "AND p.horarioInicio <= :hora " +
           "AND p.horarioFim >= :hora " +
           "AND p.id NOT IN (" +
           "    SELECT DISTINCT a.profissional.id FROM Agendamento a " +
           "    WHERE a.dataHora <= :fimJanela " +
           "    AND (a.dataHora + (a.duracaoEstimada * 1 minute)) >= :inicioJanela " +
           "    AND a.status != 'CANCELADO'" +
           ")")
    List<Profissional> findProfissionaisDisponiveisNoHorario(
        @Param("hora") LocalTime hora,
        @Param("inicioJanela") LocalDateTime inicioJanela,
        @Param("fimJanela") LocalDateTime fimJanela
    );

    /**
     * Buscar profissionais com mais agendamentos (ranking)
     */
    @Query("SELECT p, COUNT(a) as totalAgendamentos, SUM(a.precoTotal) as totalFaturamento " +
           "FROM Profissional p " +
           "LEFT JOIN p.agendamentos a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "GROUP BY p " +
           "ORDER BY totalFaturamento DESC")
    List<Object[]> findRankingProfissionais(Pageable pageable);

    /**
     * Buscar profissionais com comissão acima do valor informado
     */
    List<Profissional> findByComissaoPercentualGreaterThanEqual(BigDecimal comissao);

    /**
     * Buscar profissionais por horário de trabalho
     */
    List<Profissional> findByHorarioInicioLessThanEqualAndHorarioFimGreaterThanEqual(
        LocalTime inicio, LocalTime fim
    );

    /**
     * Buscar profissionais com maior média de avaliações
     */
    @Query("SELECT p, AVG(av.nota) as mediaNota " +
           "FROM Profissional p " +
           "LEFT JOIN p.avaliacoes av " +
           "GROUP BY p " +
           "HAVING AVG(av.nota) >= :notaMinima " +
           "ORDER BY mediaNota DESC")
    List<Object[]> findProfissionaisComMelhorAvaliacao(
        @Param("notaMinima") Double notaMinima,
        Pageable pageable
    );

    /**
     * Contar profissionais ativos por especialidade
     */
    @Query("SELECT p.especialidades, COUNT(p) FROM Profissional p " +
           "WHERE p.ativo = true " +
           "GROUP BY p.especialidades")
    List<Object[]> countProfissionaisPorEspecialidade();

    /**
     * Buscar profissionais sem agendamentos em um período
     */
    @Query("SELECT p FROM Profissional p " +
           "WHERE p.ativo = true " +
           "AND p.id NOT IN (" +
           "    SELECT DISTINCT a.profissional.id FROM Agendamento a " +
           "    WHERE a.dataHora BETWEEN :inicio AND :fim " +
           "    AND a.status != 'CANCELADO'" +
           ")")
    List<Profissional> findProfissionaisSemAgendamentosNoPeriodo(
        @Param("inicio") java.time.LocalDateTime inicio,
        @Param("fim") java.time.LocalDateTime fim
    );
}