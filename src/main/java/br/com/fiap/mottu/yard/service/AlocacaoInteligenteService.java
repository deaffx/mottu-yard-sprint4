package br.com.fiap.mottu.yard.service;

import br.com.fiap.mottu.yard.dto.OcupacaoSetor;
import br.com.fiap.mottu.yard.dto.VagaSugerida;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.repository.MotoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlocacaoInteligenteService {

    private final MotoRepository motoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Sugere a melhor vaga para uma moto baseado em regras de negócio
    public VagaSugerida sugerirMelhorVaga(Moto moto, Patio patio) {
        Map<String, Integer> configuracao = parseConfiguracaoSetores(patio.getConfiguracaoSetores());
        Map<String, OcupacaoSetor> ocupacaoPorSetor = calcularOcupacaoPorSetor(patio, configuracao);

        // Regra 1: Motos NA_OFICINA ou PARA_MANUTENCAO vão para setor da oficina
        if (moto.getStatusMoto() == Moto.StatusMoto.NA_OFICINA || 
            moto.getStatusMoto() == Moto.StatusMoto.PARA_MANUTENCAO) {
            
            String setorOficina = patio.getSetorOficina() != null ? patio.getSetorOficina() : "D";
            VagaSugerida vaga = encontrarVagaLivre(setorOficina, patio, ocupacaoPorSetor);
            
            if (vaga != null) {
                vaga.setMotivo("Setor próximo à oficina (moto precisa de manutenção)");
                vaga.setPrioridade(1);
                return vaga;
            }
        }

        // Regra 2: Motos PARA_ALUGAR vão para setor de saída rápida
        if (moto.getStatusMoto() == Moto.StatusMoto.PARA_ALUGAR) {
            String setorSaida = patio.getSetorSaidaRapida() != null ? patio.getSetorSaidaRapida() : "A";
            VagaSugerida vaga = encontrarVagaLivre(setorSaida, patio, ocupacaoPorSetor);
            
            if (vaga != null) {
                vaga.setMotivo("Setor de saída rápida (moto pronta para alugar)");
                vaga.setPrioridade(1);
                return vaga;
            }
        }

        // Regra 3: Distribuir equilibradamente nos demais casos
        return encontrarVagaEquilibrada(patio, ocupacaoPorSetor, configuracao);
    }

    // Calcula ocupação de cada setor
    public Map<String, OcupacaoSetor> calcularOcupacaoPorSetor(Patio patio, Map<String, Integer> configuracao) {
        List<Moto> motos = motoRepository.findByPatioAtualAndDeletedAtIsNull(patio);
        
        Map<String, OcupacaoSetor> resultado = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : configuracao.entrySet()) {
            String setor = entry.getKey();
            Integer capacidade = entry.getValue();
            
            long motosNoSetor = motos.stream()
                    .filter(m -> setor.equals(m.getSetor()))
                    .count();
            
            double percentual = capacidade > 0 ? (motosNoSetor * 100.0 / capacidade) : 0;
            
            OcupacaoSetor ocupacao = new OcupacaoSetor();
            ocupacao.setSetor(setor);
            ocupacao.setMotosOcupadas((int) motosNoSetor);
            ocupacao.setCapacidadeSetor(capacidade);
            ocupacao.setPercentualOcupacao(percentual);
            ocupacao.setStatus(ocupacao.getStatusLabel());
            
            resultado.put(setor, ocupacao);
        }
        
        return resultado;
    }

    // Encontra vaga livre em um setor específico
    private VagaSugerida encontrarVagaLivre(String setor, Patio patio, Map<String, OcupacaoSetor> ocupacao) {
        OcupacaoSetor ocupacaoSetor = ocupacao.get(setor);
        
        if (ocupacaoSetor == null || ocupacaoSetor.getMotosOcupadas() >= ocupacaoSetor.getCapacidadeSetor()) {
            return null; // Setor lotado
        }

        // Buscar vagas ocupadas no setor
        List<Moto> motosNoSetor = motoRepository.findByPatioAtualAndDeletedAtIsNull(patio)
                .stream()
                .filter(m -> setor.equals(m.getSetor()))
                .collect(Collectors.toList());

        Set<Integer> vagasOcupadas = motosNoSetor.stream()
                .map(Moto::getVaga)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Encontrar primeira vaga livre
        for (int i = 1; i <= ocupacaoSetor.getCapacidadeSetor(); i++) {
            if (!vagasOcupadas.contains(i)) {
                VagaSugerida sugestao = new VagaSugerida();
                sugestao.setSetor(setor);
                sugestao.setVaga(i);
                sugestao.setPercentualOcupacaoSetor(ocupacaoSetor.getPercentualOcupacao());
                return sugestao;
            }
        }

        return null;
    }

    // Encontra vaga em setor com menor ocupação (balanceamento)
    private VagaSugerida encontrarVagaEquilibrada(Patio patio, 
                                                   Map<String, OcupacaoSetor> ocupacao,
                                                   Map<String, Integer> configuracao) {
        
        // Ordenar setores por ocupação (menor para maior)
        List<Map.Entry<String, OcupacaoSetor>> setoresOrdenados = ocupacao.entrySet()
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue().getPercentualOcupacao()))
                .collect(Collectors.toList());

        // Tentar encontrar vaga no setor menos ocupado
        for (Map.Entry<String, OcupacaoSetor> entry : setoresOrdenados) {
            String setor = entry.getKey();
            VagaSugerida vaga = encontrarVagaLivre(setor, patio, ocupacao);
            
            if (vaga != null) {
                vaga.setMotivo(String.format("Balanceamento de ocupação (setor %.1f%% cheio)", 
                        vaga.getPercentualOcupacaoSetor()));
                vaga.setPrioridade(2);
                return vaga;
            }
        }

        return null; // Pátio completamente lotado
    }

    // Parse da configuração JSON de setores
    private Map<String, Integer> parseConfiguracaoSetores(String json) {
        if (json == null || json.isBlank()) {
            // Configuração padrão
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

    // Gera lista de recomendações de reorganização
    public List<String> gerarRecomendacoesReorganizacao(Patio patio) {
        Map<String, Integer> configuracao = parseConfiguracaoSetores(patio.getConfiguracaoSetores());
        Map<String, OcupacaoSetor> ocupacao = calcularOcupacaoPorSetor(patio, configuracao);
        
        List<String> recomendacoes = new ArrayList<>();

        // Verificar setores lotados
        ocupacao.forEach((setor, ocp) -> {
            if (ocp.getPercentualOcupacao() >= 90) {
                recomendacoes.add(String.format(
                    "⚠️ Setor %s está em %.1f%% de ocupação (%d/%d motos). Considere redistribuir.",
                    setor, ocp.getPercentualOcupacao(), ocp.getMotosOcupadas(), ocp.getCapacidadeSetor()
                ));
            }
        });

        // Verificar desequilíbrio entre setores
        DoubleSummaryStatistics stats = ocupacao.values().stream()
                .mapToDouble(OcupacaoSetor::getPercentualOcupacao)
                .summaryStatistics();

        double diferencaMaxima = stats.getMax() - stats.getMin();
        if (diferencaMaxima > 30) {
            recomendacoes.add(String.format(
                "📊 Desequilíbrio detectado: diferença de %.1f%% entre setores mais e menos ocupados. Recomenda-se balanceamento.",
                diferencaMaxima
            ));
        }

        // Alertas de capacidade geral
        long totalMotos = ocupacao.values().stream()
                .mapToLong(OcupacaoSetor::getMotosOcupadas)
                .sum();
        long capacidadeTotal = ocupacao.values().stream()
                .mapToLong(OcupacaoSetor::getCapacidadeSetor)
                .sum();
        double ocupacaoGeral = (totalMotos * 100.0) / capacidadeTotal;

        if (ocupacaoGeral >= 85) {
            recomendacoes.add(String.format(
                "🚨 ATENÇÃO: Pátio em %.1f%% de ocupação (%d/%d motos). Capacidade crítica!",
                ocupacaoGeral, totalMotos, capacidadeTotal
            ));
        } else if (ocupacaoGeral >= 75) {
            recomendacoes.add(String.format(
                "⚠️ Pátio em %.1f%% de ocupação (%d/%d motos). Monitore a capacidade.",
                ocupacaoGeral, totalMotos, capacidadeTotal
            ));
        }

        if (recomendacoes.isEmpty()) {
            recomendacoes.add("✅ Pátio bem organizado. Nenhuma ação necessária no momento.");
        }

        return recomendacoes;
    }
}
