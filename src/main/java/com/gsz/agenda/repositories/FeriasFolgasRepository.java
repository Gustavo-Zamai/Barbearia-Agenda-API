package com.gsz.agenda.repositories;

import com.gsz.agenda.enums.TipoFeriasFolga;
import com.gsz.agenda.Model.FeriasFolgas;
import com.gsz.agenda.Model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeriasFolgasRepository extends JpaRepository<FeriasFolgas, Integer> {

    /**
     * Buscar férias/folgas de um profissional
     */
    List<FeriasFolgas> findByProfissionalOrderByDataInicioDesc(Profissional profissional);

    /**
     * Buscar férias/folgas de um profissional no período
     */
    @Query("SELECT f FROM FeriasFolgas f " +
           "WHERE f.profissional = :profissional " +
           "AND ((f.dataInicio BETWEEN :inicio AND :fim) " +
           "OR (f.dataFim BETWEEN :inicio AND :fim) " +
           "OR (f.dataInicio <= :inicio AND f.dataFim >= :fim)) " +
           "AND f.aprovado = true")
    List<FeriasFolgas> findFeriasFolgasNoPeriodo(
        @Param("profissional") Profissional profissional,
        @Param("inicio") LocalDate inicio,
        @Param("fim") LocalDate fim
    );

    /**
     * Verificar se um profissional está de férias/folga em uma data
     */
    @Query("SELECT COUNT(f) > 0 FROM FeriasFolgas f " +
           "WHERE f.profissional = :profissional " +
           "AND :data BETWEEN f.dataInicio AND f.dataFim " +
           "AND f.aprovado = true")
    boolean isProfissionalEmFeriasFolga(
        @Param("profissional") Profissional profissional,
        @Param("data") LocalDate data
    );

    /**
     * Buscar férias/folgas por tipo
     */
    List<FeriasFolgas> findByTipoAndAprovadoTrueOrderByDataInicioDesc(TipoFeriasFolga tipo);

    /**
     * Buscar férias/folgas pendentes de aprovação
     */
    List<FeriasFolgas> findByAprovadoFalseOrderByDataInicioAsc();
}