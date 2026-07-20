package com.gsz.agenda.repositories;

import com.gsz.agenda.Model.Avaliacao;
import com.gsz.agenda.Model.Cliente;
import com.gsz.agenda.Model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    /**
     * Buscar avaliação por agendamento
     */
    Optional<Avaliacao> findByAgendamentoId(Integer agendamentoId);

    /**
     * Buscar avaliações de um profissional
     */
    List<Avaliacao> findByProfissionalOrderByCreatedAtDesc(Profissional profissional);

    /**
     * Buscar avaliações de um cliente
     */
    List<Avaliacao> findByClienteOrderByCreatedAtDesc(Cliente cliente);

    /**
     * Buscar avaliações de um profissional com nota mínima
     */
    List<Avaliacao> findByProfissionalAndNotaGreaterThanEqual(
        Profissional profissional,
        Integer nota
    );

    /**
     * Calcular média de avaliações de um profissional
     */
    @Query("SELECT AVG(a.nota) FROM Avaliacao a " +
           "WHERE a.profissional = :profissional")
    Double calcularMediaAvaliacoesProfissional(@Param("profissional") Profissional profissional);

    /**
     * Contar avaliações por nota
     */
    @Query("SELECT a.nota, COUNT(a) FROM Avaliacao a " +
           "WHERE a.profissional = :profissional " +
           "GROUP BY a.nota")
    List<Object[]> countAvaliacoesPorNota(@Param("profissional") Profissional profissional);

    /**
     * Buscar últimas avaliações (limitado)
     */
    @Query("SELECT a FROM Avaliacao a " +
           "ORDER BY a.createdAt DESC")
    List<Avaliacao> findUltimasAvaliacoes(org.springframework.data.domain.Pageable pageable);

    /**
     * Verificar se um agendamento já tem avaliação
     */
    boolean existsByAgendamentoId(Integer agendamentoId);
}