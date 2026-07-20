package com.gsz.agenda.repositories;

import com.gsz.agenda.Model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // ====== BUSCAS BÁSICAS ======
    
    /**
     * Buscar cliente por email
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Buscar cliente por email (ignorando maiúsculas/minúsculas)
     */
    Optional<Cliente> findByEmailIgnoreCase(String email);

    /**
     * Buscar cliente por telefone
     */
    Optional<Cliente> findByTelefone(String telefone);

    // ====== VERIFICAÇÕES ======

    /**
     * Verificar se existe cliente com este email
     */
    boolean existsByEmail(String email);

    /**
     * Verificar se existe cliente com este email (ignorando ID)
     */
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.email = :email AND c.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Integer id);

    /**
     * Verificar se existe cliente com este telefone
     */
    boolean existsByTelefone(String telefone);

    /**
     * Verificar se existe cliente com este telefone (ignorando ID)
     */
    @Query("SELECT COUNT(c) > 0 FROM Cliente c WHERE c.telefone = :telefone AND c.id != :id")
    boolean existsByTelefoneAndIdNot(@Param("telefone") String telefone, @Param("id") Integer id);

    // ====== LISTAGENS ======

    /**
     * Listar todos os clientes ativos
     */
    List<Cliente> findByAtivoTrue();

    /**
     * Listar todos os clientes inativos
     */
    List<Cliente> findByAtivoFalse();

    /**
     * Listar clientes ativos com paginação
     */
    Page<Cliente> findByAtivoTrue(Pageable pageable);

    /**
     * Buscar clientes por nome (contém)
     */
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    /**
     * Buscar clientes por nome com paginação
     */
    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Buscar clientes por email (contém)
     */
    List<Cliente> findByEmailContainingIgnoreCase(String email);

    /**
     * Buscar clientes por telefone (contém)
     */
    List<Cliente> findByTelefoneContaining(String telefone);

    // ====== CONSULTAS PERSONALIZADAS ======

    /**
     * Buscar clientes que fizeram agendamentos em um período
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "JOIN c.agendamentos a " +
           "WHERE a.dataHora BETWEEN :inicio AND :fim " +
           "AND a.status != 'CANCELADO'")
    List<Cliente> findClientesComAgendamentosNoPeriodo(
        @Param("inicio") java.time.LocalDateTime inicio,
        @Param("fim") java.time.LocalDateTime fim
    );

    /**
     * Buscar clientes com mais agendamentos (ranking)
     */
    @Query("SELECT c, COUNT(a) as totalAgendamentos " +
           "FROM Cliente c " +
           "JOIN c.agendamentos a " +
           "WHERE a.status = 'CONCLUIDO' " +
           "GROUP BY c " +
           "ORDER BY totalAgendamentos DESC")
    List<Object[]> findTopClientesPorAgendamentos(Pageable pageable);

    /**
     * Buscar clientes que nunca agendaram
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.id NOT IN (SELECT DISTINCT a.cliente.id FROM Agendamento a)")
    List<Cliente> findClientesSemAgendamentos();

    /**
     * Buscar clientes por data de criação (hoje)
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE DATE(c.createdAt) = CURRENT_DATE")
    List<Cliente> findClientesCriadosHoje();

    /**
     * Buscar clientes por período de criação
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.createdAt BETWEEN :inicio AND :fim")
    List<Cliente> findClientesCriadosNoPeriodo(
        @Param("inicio") java.time.LocalDateTime inicio,
        @Param("fim") java.time.LocalDateTime fim
    );

    /**
     * Contar clientes ativos por gênero
     */
    @Query("SELECT c.genero, COUNT(c) FROM Cliente c " +
           "WHERE c.ativo = true " +
           "GROUP BY c.genero")
    List<Object[]> countClientesPorGenero();

    /**
     * Buscar clientes com aniversário no mês
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE MONTH(c.dataNascimento) = :mes")
    List<Cliente> findClientesAniversariantesDoMes(@Param("mes") Integer mes);

    /**
     * Buscar clientes por faixa etária
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE YEAR(CURRENT_DATE) - YEAR(c.dataNascimento) BETWEEN :idadeMin AND :idadeMax")
    List<Cliente> findClientesPorFaixaEtaria(
        @Param("idadeMin") Integer idadeMin,
        @Param("idadeMax") Integer idadeMax
    );
}