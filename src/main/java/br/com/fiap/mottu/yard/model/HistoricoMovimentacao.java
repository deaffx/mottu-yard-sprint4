package br.com.fiap.mottu.yard.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_movimentacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoMovimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "moto_id", nullable = false)
    private Moto moto;

    @ManyToOne
    @JoinColumn(name = "patio_origem_id")
    private Patio patioOrigem;

    @Column(name = "setor_origem")
    private String setorOrigem;

    @Column(name = "vaga_origem")
    private Integer vagaOrigem;

    @ManyToOne
    @JoinColumn(name = "patio_destino_id")
    private Patio patioDestino;

    @Column(name = "setor_destino")
    private String setorDestino;

    @Column(name = "vaga_destino")
    private Integer vagaDestino;

    @Column(name = "motivo")
    private String motivo;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "data_movimentacao")
    private LocalDateTime dataMovimentacao;

    @PrePersist
    protected void onCreate() {
        if (dataMovimentacao == null) {
            dataMovimentacao = LocalDateTime.now();
        }
    }

    public String getLocalizacaoOrigemCompleta() {
        if (setorOrigem != null && vagaOrigem != null) {
            return String.format("%s-%02d", setorOrigem, vagaOrigem);
        }
        return "N/A";
    }

    public String getLocalizacaoDestinoCompleta() {
        if (setorDestino != null && vagaDestino != null) {
            return String.format("%s-%02d", setorDestino, vagaDestino);
        }
        return "N/A";
    }
}
