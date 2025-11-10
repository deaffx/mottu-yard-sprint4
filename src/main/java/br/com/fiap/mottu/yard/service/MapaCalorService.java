package br.com.fiap.mottu.yard.service;

import br.com.fiap.mottu.yard.dto.MapaCalor;
import br.com.fiap.mottu.yard.dto.OcupacaoSetor;
import br.com.fiap.mottu.yard.model.HistoricoMovimentacao;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.repository.HistoricoMovimentacaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapaCalorService {

    private final AlocacaoInteligenteService alocacaoService;
    private final HistoricoMovimentacaoRepository historicoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Gera mapa de calor completo do pátio
    public MapaCalor gerarMapaCalor(Patio patio) {
        Map<String, Integer> configuracao = parseConfiguracaoSetores(patio.getConfiguracaoSetores());
        Map<String, OcupacaoSetor> ocupacaoPorSetor = alocacaoService.calcularOcupacaoPorSetor(patio, configuracao);

        // Garantir que sempre temos os 4 setores (A, B, C, D) na ordem correta
        List<String> setoresPadrao = Arrays.asList("A", "B", "C", "D");
        List<OcupacaoSetor> setoresOrdenados = new ArrayList<>();
        
        for (String setor : setoresPadrao) {
            OcupacaoSetor ocupacao = ocupacaoPorSetor.get(setor);
            if (ocupacao != null) {
                setoresOrdenados.add(ocupacao);
            } else {
                // Se o setor não existe na configuração, criar um vazio
                OcupacaoSetor setorVazio = new OcupacaoSetor();
                setorVazio.setSetor(setor);
                setorVazio.setMotosOcupadas(0);
                setorVazio.setCapacidadeSetor(0);
                setorVazio.setPercentualOcupacao(0.0);
                setorVazio.setStatus("N/A");
                setoresOrdenados.add(setorVazio);
            }
        }

        int totalMotos = setoresOrdenados.stream()
                .mapToInt(OcupacaoSetor::getMotosOcupadas)
                .sum();

        int capacidadeTotal = setoresOrdenados.stream()
                .mapToInt(OcupacaoSetor::getCapacidadeSetor)
                .sum();

        double percentualGeral = capacidadeTotal > 0 ? (totalMotos * 100.0 / capacidadeTotal) : 0;

        MapaCalor mapa = new MapaCalor();
        mapa.setPatioId(patio.getId());
        mapa.setPatioNome(patio.getNome());
        mapa.setSetores(setoresOrdenados);
        mapa.setTotalMotos(totalMotos);
        mapa.setCapacidadeTotal(capacidadeTotal);
        mapa.setPercentualOcupacaoGeral(percentualGeral);

        return mapa;
    }

    // Registra movimentação no histórico
    @Transactional
    public void registrarMovimentacao(Moto moto, 
                                       Patio patioOrigem, String setorOrigem, Integer vagaOrigem,
                                       Patio patioDestino, String setorDestino, Integer vagaDestino,
                                       String motivo) {
        
        HistoricoMovimentacao historico = new HistoricoMovimentacao();
        historico.setMoto(moto);
        historico.setPatioOrigem(patioOrigem);
        historico.setSetorOrigem(setorOrigem);
        historico.setVagaOrigem(vagaOrigem);
        historico.setPatioDestino(patioDestino);
        historico.setSetorDestino(setorDestino);
        historico.setVagaDestino(vagaDestino);
        historico.setMotivo(motivo);
        historico.setUsuario(getUsuarioLogado());

        historicoRepository.save(historico);
        
        log.info("Movimentação registrada: Moto {} de {}-{} para {}-{}", 
                moto.getPlaca(), 
                setorOrigem, vagaOrigem,
                setorDestino, vagaDestino);
    }

    // Busca histórico de uma moto
    public List<HistoricoMovimentacao> buscarHistoricoMoto(Moto moto) {
        return historicoRepository.findByMotoOrderByDataMovimentacaoDesc(moto);
    }

    // Busca histórico de movimentações
    private Map<String, Integer> parseConfiguracaoSetores(String json) {
        if (json == null || json.isBlank()) {
            Map<String, Integer> padrao = new HashMap<>();
            padrao.put("A", 25);
            padrao.put("B", 25);
            padrao.put("C", 25);
            padrao.put("D", 25);
            return padrao;
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.error("Erro ao parsear configuração de setores: {}", json, e);
            return new HashMap<>();
        }
    }

    // Obtém usuário logado do contexto de segurança
    private String getUsuarioLogado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
                return oauth2User.getAttribute("login");
            }
            return "sistema";
        } catch (Exception e) {
            return "sistema";
        }
    }
}
