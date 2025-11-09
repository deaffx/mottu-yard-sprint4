package br.com.fiap.mottu.yard.repository;

import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotoRepository extends JpaRepository<Moto, Long> {
    Optional<Moto> findByPlacaAndDeletedAtIsNull(String placa);
    
    List<Moto> findByDeletedAtIsNull();
    
    Optional<Moto> findByIdAndDeletedAtIsNull(Long id);
    
    List<Moto> findByPatioAtualAndDeletedAtIsNull(Patio patio);
    
    List<Moto> findByStatusMotoAndDeletedAtIsNull(Moto.StatusMoto status);
    
    List<Moto> findByMarcaContainingIgnoreCaseAndDeletedAtIsNull(String marca);
    
    List<Moto> findByModeloContainingIgnoreCaseAndDeletedAtIsNull(String modelo);
    
    Page<Moto> findByDeletedAtIsNull(Pageable pageable);
    
    Page<Moto> findByModeloContainingIgnoreCaseOrMarcaContainingIgnoreCaseOrPlacaContainingIgnoreCaseAndDeletedAtIsNull(
        String modelo, String marca, String placa, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Moto m WHERE m.patioAtual = :patio AND m.deletedAt IS NULL")
    Long countMotosInPatio(@Param("patio") Patio patio);
    
    @Query("SELECT COUNT(m) FROM Moto m WHERE m.deletedAt IS NULL")
    Long countActiveMotos();
}