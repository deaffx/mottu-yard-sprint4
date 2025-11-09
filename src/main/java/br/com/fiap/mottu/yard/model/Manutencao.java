package br.com.fiap.mottu.yard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "manutencoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "moto_id", nullable = false)
    private Moto moto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_manutencao", nullable = false)
    private TipoManutencao tipoManutencao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_manutencao", nullable = false)
    private StatusManutencao statusManutencao = StatusManutencao.AGENDADA;

    @Column(name = "data_agendada")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataAgendada;

    @Column(name = "data_iniciada")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataIniciada;

    @Column(name = "data_concluida")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataConcluida;

    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Positive
    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Size(max = 1000)
    @Column(name = "pecas_utilizadas", columnDefinition = "TEXT")
    private String pecasUtilizadas;

    public enum TipoManutencao {
        PREVENTIVA("Preventiva"),
        CORRETIVA("Corretiva"),
        REVISAO("Revisão"),
        EMERGENCIAL("Emergencial");

        private final String label;

        TipoManutencao(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum StatusManutencao {
        AGENDADA("Agendada"),
        EM_ANDAMENTO("Em andamento"),
        CONCLUIDA("Concluída");

        private final String label;

        StatusManutencao(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}