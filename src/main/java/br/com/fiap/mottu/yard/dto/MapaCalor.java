package br.com.fiap.mottu.yard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapaCalor {
    private Long patioId;
    private String patioNome;
    private java.util.List<OcupacaoSetor> setores;
    private Integer totalMotos;
    private Integer capacidadeTotal;
    private Double percentualOcupacaoGeral;
}
