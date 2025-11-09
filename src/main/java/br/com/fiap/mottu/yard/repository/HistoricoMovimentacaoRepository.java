package br.com.fiap.mottu.yard.repository;

import br.com.fiap.mottu.yard.model.HistoricoMovimentacao;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoMovimentacaoRepository extends JpaRepository<HistoricoMovimentacao, Long> {
    
    List<HistoricoMovimentacao> findByMotoOrderByDataMovimentacaoDesc(Moto moto);
    
    List<HistoricoMovimentacao> findByPatioDestinoOrderByDataMovimentacaoDesc(Patio patio);
    
    @Query("SELECT h FROM HistoricoMovimentacao h WHERE h.dataMovimentacao BETWEEN :inicio AND :fim ORDER BY h.dataMovimentacao DESC")
    List<HistoricoMovimentacao> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT h FROM HistoricoMovimentacao h WHERE h.usuario = :usuario ORDER BY h.dataMovimentacao DESC")
    List<HistoricoMovimentacao> findByUsuario(@Param("usuario") String usuario);
}
