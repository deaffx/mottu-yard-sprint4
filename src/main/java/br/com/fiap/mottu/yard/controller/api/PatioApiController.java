package br.com.fiap.mottu.yard.controller.api;

import br.com.fiap.mottu.yard.dto.MapaCalor;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.service.MapaCalorService;
import br.com.fiap.mottu.yard.service.MotoService;
import br.com.fiap.mottu.yard.service.PatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patios")
@RequiredArgsConstructor
public class PatioApiController {

    private final PatioService patioService;
    private final MapaCalorService mapaCalorService;
    private final MotoService motoService;

    @GetMapping("/{id}/mapa")
    public ResponseEntity<?> getMapaPatio(@PathVariable("id") Long id) {
        return patioService.findById(id)
                .map(patio -> {
                    MapaCalor mapa = mapaCalorService.gerarMapaCalor(patio);
                    
                    // Buscar vagas ocupadas por setor
                    List<Moto> motos = motoService.findByPatio(patio);
                    Map<String, List<Integer>> vagasOcupadas = motos.stream()
                            .filter(m -> m.getSetor() != null && m.getVaga() != null)
                            .collect(Collectors.groupingBy(
                                    Moto::getSetor,
                                    Collectors.mapping(Moto::getVaga, Collectors.toList())
                            ));
                    
                    // Criar resposta
                    Map<String, Object> response = new HashMap<>();
                    response.put("setores", mapa.getSetores());
                    response.put("vagasOcupadas", vagasOcupadas);
                    response.put("totalMotos", mapa.getTotalMotos());
                    response.put("capacidadeTotal", mapa.getCapacidadeTotal());
                    
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ocupacao")
    public ResponseEntity<?> getPatiosComOcupacao() {
        var patios = patioService.findAll();
        
        List<Map<String, Object>> patiosData = patios.stream()
                .map(patio -> {
                    long ocupacao = motoService.findByPatio(patio).size();
                    double percentual = patio.getCapacidadeMaxima() > 0 
                        ? (ocupacao * 100.0 / patio.getCapacidadeMaxima()) 
                        : 0.0;
                    
                    Map<String, Object> data = new HashMap<>();
                    
                    // Dados do pátio
                    Map<String, Object> patioData = new HashMap<>();
                    patioData.put("id", patio.getId());
                    patioData.put("nome", patio.getNome());
                    patioData.put("endereco", patio.getEndereco());
                    patioData.put("capacidadeMaxima", patio.getCapacidadeMaxima());
                    
                    data.put("patio", patioData);
                    data.put("ocupacao", ocupacao);
                    data.put("percentual", percentual);
                    
                    return data;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(patiosData);
    }
}
