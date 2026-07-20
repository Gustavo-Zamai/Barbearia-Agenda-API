package com.gsz.agenda.repositories;

import com.gsz.agenda.Model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    // ====== BUSCAS BÁSICAS ======

    /**
     * Buscar serviço por nome
     */
    List<Servico> findByNome(String nome);

    /**
     * Buscar serviço por nome (ignorando maiúsculas/minúsculas)
     */
    List<Servico> findByNomeIgnoreCase(String nome);

    /**
     * Verificar se existe serviço com este nome
     */
    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Verificar se existe serviço com este nome (ignorando ID)
     */
    @Query("SELECT COUNT(s) > 0 FROM Servico s WHERE s.nome = :nome AND s.id != :id")
    boolean existsByNomeAndIdNot(@Param("nome") String nome, @Param("id") Integer id);

    // ====== LISTAGENS ======

    /**
     * Listar todos os serviços ativos
     */
    List<Servico> findByAtivoTrue();

    /**
     * Listar todos os serviços inativos
     */
    List<Servico> findByAtivoFalse();

    /**
     * Listar serviços ativos com paginação
     */
    Page<Servico> findByAtivoTrue(Pageable pageable);

    /**
     * Buscar serviços por categoria
     */
    List<Servico> findByCategoriaIgnoreCase(String categoria);

    /**
     * Buscar serviços por categoria (apenas ativos)
     */
    List<Servico> findByCategoriaIgnoreCaseAndAtivoTrue(String categoria);

    /**
     * Buscar serviços por nome (contém)
     */
    List<Servico> findByNomeContainingIgnoreCase(String nome);

    /**
     * Buscar serviços por descrição (contém)
     */
    List<Servico> findByDescricaoContainingIgnoreCase(String descricao);

    // ====== CONSULTAS PERSONALIZADAS ======

    /**
     * Buscar serviços mais agendados
     */
    @Query("SELECT s, COUNT(a) as totalAgendamentos " +
           "FROM Servico s " +
           "JOIN s.agendamentos a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "GROUP BY s " +
           "ORDER BY totalAgendamentos DESC")
    List<Object[]> findServicosMaisAgendados(Pageable pageable);

    /**
     * Buscar serviços por faixa de preço
     */
    List<Servico> findByPrecoBetween(BigDecimal precoMin, BigDecimal precoMax);

    /**
     * Buscar serviços com preço promocional
     */
    List<Servico> findByPrecoPromocionalIsNotNull();

    /**
     * Buscar serviços com preço promocional ativo
     */
    @Query("SELECT s FROM Servico s " +
           "WHERE s.precoPromocional IS NOT NULL " +
           "AND s.ativo = true " +
           "AND s.precoPromocional < s.preco")
    List<Servico> findServicosComPromocaoAtiva();

    /**
     * Buscar serviços por duração máxima
     */
    List<Servico> findByDuracaoMinutosLessThanEqual(Integer duracaoMax);

    /**
     * Buscar serviços por duração mínima
     */
    List<Servico> findByDuracaoMinutosGreaterThanEqual(Integer duracaoMin);

    /**
     * Buscar serviços com faturamento total acima do valor
     */
    @Query("SELECT s, SUM(a.precoTotal) as totalFaturamento " +
           "FROM Servico s " +
           "JOIN s.agendamentos a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "GROUP BY s " +
           "HAVING SUM(a.precoTotal) >= :valorMinimo")
    List<Object[]> findServicosComFaturamentoMinimo(
        @Param("valorMinimo") BigDecimal valorMinimo
    );

    /**
     * Buscar serviços por categoria e faixa de preço
     */
    List<Servico> findByCategoriaIgnoreCaseAndPrecoBetweenAndAtivoTrue(
        String categoria, 
        BigDecimal precoMin, 
        BigDecimal precoMax
    );

    /**
     * Buscar serviços que não têm agendamentos
     */
    @Query("SELECT s FROM Servico s " +
           "WHERE s.ativo = true " +
           "AND s.id NOT IN (SELECT DISTINCT a.servico.id FROM Agendamento a)")
    List<Servico> findServicosSemAgendamentos();

    /**
     * Buscar serviços mais lucrativos (maior margem de comissão)
     */
    @Query("SELECT s FROM Servico s " +
           "WHERE s.ativo = true " +
           "ORDER BY (s.preco * s.comissaoPercentual) DESC")
    List<Servico> findServicosMaisLucrativos(Pageable pageable);
}