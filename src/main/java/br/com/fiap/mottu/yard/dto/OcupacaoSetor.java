package br.com.fiap.mottu.yard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcupacaoSetor {
    private String setor;
    private Integer motosOcupadas;
    private Integer capacidadeSetor;
    private Double percentualOcupacao;
    private String status; // "LIVRE", "MODERADO", "CHEIO", "LOTADO"

    public String getStatusClass() {
        if (percentualOcupacao == null) return "secondary";
        if (percentualOcupacao < 50) return "success";
        if (percentualOcupacao < 75) return "info";
        if (percentualOcupacao < 90) return "warning";
        return "danger";
    }

    public String getStatusLabel() {
        if (percentualOcupacao == null) return "N/A";
        if (percentualOcupacao < 50) return "LIVRE";
        if (percentualOcupacao < 75) return "MODERADO";
        if (percentualOcupacao < 90) return "CHEIO";
        return "LOTADO";
    }
}
