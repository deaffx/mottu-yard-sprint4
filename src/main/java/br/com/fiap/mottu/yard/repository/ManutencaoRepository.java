package br.com.fiap.mottu.yard.repository;

import br.com.fiap.mottu.yard.model.Manutencao;
import br.com.fiap.mottu.yard.model.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    List<Manutencao> findByMoto(Moto moto);
    List<Manutencao> findByStatusManutencao(Manutencao.StatusManutencao status);
    List<Manutencao> findByTipoManutencao(Manutencao.TipoManutencao tipo);
    
    @Query("SELECT m FROM Manutencao m WHERE m.dataAgendada < :data AND m.statusManutencao = :status")
    List<Manutencao> findManutencaoVencida(@Param("data") LocalDateTime data, @Param("status") Manutencao.StatusManutencao status);
    
    @Query("SELECT m FROM Manutencao m WHERE m.statusManutencao IN ('AGENDADA', 'EM_ANDAMENTO') ORDER BY m.dataAgendada")
    List<Manutencao> findManutencoesPendentes();
    
    List<Manutencao> findAllByOrderByDataAgendadaDesc();
}