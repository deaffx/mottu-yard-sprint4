package br.com.fiap.mottu.yard.service;

import br.com.fiap.mottu.yard.exception.BusinessException;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.repository.MotoRepository;
import br.com.fiap.mottu.yard.repository.PatioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatioService {

    private final PatioRepository patioRepository;
    private final MotoRepository motoRepository;

    public Page<Patio> findAll(Pageable pageable) {
        return patioRepository.findAll(pageable);
    }

    public Page<Patio> search(String termo, Pageable pageable) {
        String filtro = termo == null ? "" : termo.trim();
        return patioRepository.findByNomeContainingIgnoreCaseOrEnderecoContainingIgnoreCase(
                filtro,
                filtro,
                pageable
        );
    }

    public List<Patio> findAll() {
        return patioRepository.findAll();
    }

    public Optional<Patio> findById(Long id) {
        return patioRepository.findById(id);
    }

    public Patio save(Patio patio) {
        // Adicionar valores padrão se não foram especificados
        if (patio.getConfiguracaoSetores() == null || patio.getConfiguracaoSetores().isEmpty()) {
            // Dividir a capacidade total do pátio igualmente entre os 4 setores
            int capacidadeTotal = patio.getCapacidadeMaxima() != null ? patio.getCapacidadeMaxima() : 100;
            int capacidadePorSetor = capacidadeTotal / 4;
            int resto = capacidadeTotal % 4;
            
            // Distribuir o resto entre os primeiros setores
            int capacidadeA = capacidadePorSetor + (resto > 0 ? 1 : 0);
            int capacidadeB = capacidadePorSetor + (resto > 1 ? 1 : 0);
            int capacidadeC = capacidadePorSetor + (resto > 2 ? 1 : 0);
            int capacidadeD = capacidadePorSetor;
            
            patio.setConfiguracaoSetores(
                String.format("{\"A\": %d, \"B\": %d, \"C\": %d, \"D\": %d}", 
                    capacidadeA, capacidadeB, capacidadeC, capacidadeD)
            );
        }
        if (patio.getSetorOficina() == null || patio.getSetorOficina().isEmpty()) {
            patio.setSetorOficina("D");
        }
        if (patio.getSetorSaidaRapida() == null || patio.getSetorSaidaRapida().isEmpty()) {
            patio.setSetorSaidaRapida("A");
        }
        
        return patioRepository.save(patio);
    }

    public void deleteById(Long id) {
        try {
            patioRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Não é possível excluir este pátio pois existem motos alocadas nele.", e);
        }
    }

    public List<Patio> findByNome(String nome) {
        return patioRepository.findByNomeContainingIgnoreCase(nome);
    }

    public boolean hasCapacidade(Patio patio) {
        Long motosAtuais = motoRepository.countMotosInPatio(patio);
        return motosAtuais < patio.getCapacidadeMaxima();
    }

    public Long getOcupacaoAtual(Patio patio) {
        return motoRepository.countMotosInPatio(patio);
    }

    public double getTaxaOcupacao(Patio patio) {
        Long ocupacao = getOcupacaoAtual(patio);
        Integer capacidadeMaxima = patio.getCapacidadeMaxima();
        if (capacidadeMaxima == null || capacidadeMaxima == 0) {
            return ocupacao > 0 ? 100d : 0d;
        }
        return (ocupacao.doubleValue() / capacidadeMaxima.doubleValue()) * 100;
    }
}