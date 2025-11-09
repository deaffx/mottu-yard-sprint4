package br.com.fiap.mottu.yard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VagaSugerida {
    private String setor;
    private Integer vaga;
    private String motivo;
    private Integer prioridade; // 1 = alta, 2 = média, 3 = baixa
    private Double percentualOcupacaoSetor;

    public String getLocalizacaoCompleta() {
        return String.format("%s-%02d", setor, vaga);
    }
}
