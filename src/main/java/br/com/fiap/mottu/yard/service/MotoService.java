package br.com.fiap.mottu.yard.service;

import br.com.fiap.mottu.yard.exception.BusinessException;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.repository.MotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MotoService {

    private final MotoRepository motoRepository;

    public Page<Moto> findAll(Pageable pageable) {
        return motoRepository.findByDeletedAtIsNull(pageable);
    }

    public Page<Moto> search(String termo, Pageable pageable) {
        String filtro = termo == null ? "" : termo.trim();
        return motoRepository.findByModeloContainingIgnoreCaseOrMarcaContainingIgnoreCaseOrPlacaContainingIgnoreCaseAndDeletedAtIsNull(
                filtro,
                filtro,
                filtro,
                pageable
        );
    }

    public List<Moto> findAll() {
        return motoRepository.findByDeletedAtIsNull();
    }

    public Optional<Moto> findById(Long id) {
        return motoRepository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<Moto> findByPlaca(String placa) {
        return motoRepository.findByPlacaAndDeletedAtIsNull(placa);
    }

    public Moto save(Moto moto) {
        // Validar se a vaga está ocupada antes de salvar
        if (moto.getPatioAtual() != null && moto.getSetor() != null && moto.getVaga() != null) {
            Long motoIdExcluir = moto.getId(); // null para novas motos
            if (isVagaOcupada(moto.getPatioAtual(), moto.getSetor(), moto.getVaga(), motoIdExcluir)) {
                throw new BusinessException(
                    String.format("A vaga %s-%02d já está ocupada por outra moto!", 
                        moto.getSetor(), moto.getVaga())
                );
            }
        }
        return motoRepository.save(moto);
    }

    public void deleteById(Long id) {
        Optional<Moto> motoOptional = motoRepository.findByIdAndDeletedAtIsNull(id);
        
        if (motoOptional.isEmpty()) {
            throw new BusinessException("Moto não encontrada ou já foi excluída");
        }
        
        Moto moto = motoOptional.get();
        moto.softDelete();
        
        try {
            motoRepository.save(moto);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Não é possível excluir esta moto pois está registrada em manutenção ativa", e);
        }
    }

    public void restore(Long id) {
        Optional<Moto> motoOptional = motoRepository.findById(id);
        
        if (motoOptional.isEmpty()) {
            throw new BusinessException("Moto não encontrada");
        }
        
        Moto moto = motoOptional.get();
        
        if (!moto.isDeleted()) {
            throw new BusinessException("Moto não está excluída");
        }
        
        moto.restore();
        motoRepository.save(moto);
    }

    public List<Moto> findByPatio(Patio patio) {
        return motoRepository.findByPatioAtualAndDeletedAtIsNull(patio);
    }

    public List<Moto> findByStatus(Moto.StatusMoto status) {
        return motoRepository.findByStatusMotoAndDeletedAtIsNull(status);
    }

    public List<Moto> findByMarca(String marca) {
        return motoRepository.findByMarcaContainingIgnoreCaseAndDeletedAtIsNull(marca);
    }

    public List<Moto> findByModelo(String modelo) {
        return motoRepository.findByModeloContainingIgnoreCaseAndDeletedAtIsNull(modelo);
    }

    public Long countTotalMotos() {
        return motoRepository.countActiveMotos();
    }

    public Long countMotosByStatus(Moto.StatusMoto status) {
        return (long) motoRepository.findByStatusMotoAndDeletedAtIsNull(status).size();
    }

    public List<Moto> findRecentMotos(int limit) {
        return motoRepository.findAll(
            org.springframework.data.domain.PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    // Verifica se uma vaga específica está ocupada por outra moto (exceto a moto informada)
    public boolean isVagaOcupada(Patio patio, String setor, Integer vaga, Long motoIdExcluir) {
        List<Moto> motos = findByPatio(patio);
        return motos.stream()
                .filter(m -> !m.getId().equals(motoIdExcluir)) // Excluir a própria moto
                .anyMatch(m -> setor.equals(m.getSetor()) && vaga.equals(m.getVaga()));
    }
}