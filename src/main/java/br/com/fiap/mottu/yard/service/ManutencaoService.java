package br.com.fiap.mottu.yard.service;

import br.com.fiap.mottu.yard.model.Manutencao;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.repository.ManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManutencaoService {

    private final ManutencaoRepository manutencaoRepository;
    private final MotoService motoService;

    public List<Manutencao> findAllOrderByDataAgendadaDesc() {
        return manutencaoRepository.findAllByOrderByDataAgendadaDesc();
    }

    public Optional<Manutencao> findById(Long id) {
        return manutencaoRepository.findById(id);
    }

    public Manutencao save(Manutencao manutencao) {
        return manutencaoRepository.save(manutencao);
    }

    public void deleteById(Long id) {
        manutencaoRepository.deleteById(id);
    }

    public List<Manutencao> findByMoto(Moto moto) {
        return manutencaoRepository.findByMoto(moto);
    }

    public List<Manutencao> findByStatus(Manutencao.StatusManutencao status) {
        return manutencaoRepository.findByStatusManutencao(status);
    }

    public Manutencao agendarManutencao(Manutencao manutencao) {
        // Validar se a moto não está na oficina
        Moto moto = manutencao.getMoto();
        if (moto.getStatusMoto() == Moto.StatusMoto.NA_OFICINA) {
            throw new IllegalStateException("Moto já está em manutenção");
        }

        manutencao.setStatusManutencao(Manutencao.StatusManutencao.AGENDADA);

        // Status passa a indicar que precisa ser enviada para manutenção
        moto.setStatusMoto(Moto.StatusMoto.PARA_MANUTENCAO);
        motoService.save(moto);

        return manutencaoRepository.save(manutencao);
    }

    public Manutencao iniciarManutencao(Long manutencaoId) {
        Manutencao manutencao = manutencaoRepository.findById(manutencaoId)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada"));

        manutencao.setStatusManutencao(Manutencao.StatusManutencao.EM_ANDAMENTO);
        manutencao.setDataIniciada(LocalDateTime.now());

        // Atualizar status da moto
        Moto moto = manutencao.getMoto();
        moto.setStatusMoto(Moto.StatusMoto.NA_OFICINA);
        motoService.save(moto);

        return manutencaoRepository.save(manutencao);
    }

    public Manutencao concluirManutencao(Long manutencaoId) {
        Manutencao manutencao = manutencaoRepository.findById(manutencaoId)
                .orElseThrow(() -> new RuntimeException("Manutenção não encontrada"));

        manutencao.setStatusManutencao(Manutencao.StatusManutencao.CONCLUIDA);
        manutencao.setDataConcluida(LocalDateTime.now());

        // Atualizar status da moto para disponível
        Moto moto = manutencao.getMoto();
        moto.setStatusMoto(Moto.StatusMoto.PARA_ALUGAR);
        motoService.save(moto);

        return manutencaoRepository.save(manutencao);
    }

    public List<Manutencao> findManutencoesPendentes() {
        return manutencaoRepository.findManutencoesPendentes();
    }

    public List<Manutencao> findManutencaoVencidas() {
        return manutencaoRepository.findManutencaoVencida(
                LocalDateTime.now(), 
                Manutencao.StatusManutencao.AGENDADA
        );
    }

    public Long countManutencoesByStatus(Manutencao.StatusManutencao status) {
        return (long) manutencaoRepository.findByStatusManutencao(status).size();
    }
}