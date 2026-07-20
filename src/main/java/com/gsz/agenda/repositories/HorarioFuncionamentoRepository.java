package com.gsz.agenda.repositories;

import com.gsz.agenda.enums.DiaSemana;
import com.gsz.agenda.Model.HorarioFuncionamento;
import com.gsz.agenda.Model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioFuncionamentoRepository extends JpaRepository<HorarioFuncionamento, Integer> {

    /**
     * Buscar horários de um profissional
     */
    List<HorarioFuncionamento> findByProfissionalOrderByDiaSemana(Profissional profissional);

    /**
     * Buscar horários ativos de um profissional
     */
    List<HorarioFuncionamento> findByProfissionalAndAtivoTrueOrderByDiaSemana(Profissional profissional);

    /**
     * Buscar horário de um profissional em um dia específico
     */
    Optional<HorarioFuncionamento> findByProfissionalAndDiaSemana(
        Profissional profissional,
        DiaSemana diaSemana
    );

    /**
     * Buscar profissionais disponíveis em um dia e horário
     */
    @Query("SELECT h.profissional FROM HorarioFuncionamento h " +
           "WHERE h.diaSemana = :diaSemana " +
           "AND h.horarioInicio <= :hora " +
           "AND h.horarioFim >= :hora " +
           "AND h.ativo = true " +
           "AND h.profissional.ativo = true")
    List<Profissional> findProfissionaisDisponiveisNoDiaHora(
        @Param("diaSemana") DiaSemana diaSemana,
        @Param("hora") LocalTime hora
    );

    /**
     * Verificar se um profissional trabalha em um dia
     */
    @Query("SELECT COUNT(h) > 0 FROM HorarioFuncionamento h " +
           "WHERE h.profissional = :profissional " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.ativo = true")
    boolean isProfissionalTrabalhaNoDia(
        @Param("profissional") Profissional profissional,
        @Param("diaSemana") DiaSemana diaSemana
    );
}