package br.com.fiap.mottu.yard.repository;

import br.com.fiap.mottu.yard.model.Patio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatioRepository extends JpaRepository<Patio, Long> {
    List<Patio> findByNomeContainingIgnoreCase(String nome);
    Page<Patio> findByNomeContainingIgnoreCaseOrEnderecoContainingIgnoreCase(String nome, String endereco, Pageable pageable);
    
    @Query("SELECT p FROM Patio p WHERE p.capacidadeMaxima > :capacidade")
    List<Patio> findByCapacidadeMaximaGreaterThan(Integer capacidade);
}