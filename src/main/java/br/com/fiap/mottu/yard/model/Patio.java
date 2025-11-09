package br.com.fiap.mottu.yard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "patios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String endereco;

    @NotNull
    @Positive
    @Column(name = "capacidade_maxima", nullable = false)
    private Integer capacidadeMaxima;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // Campos de configuração de setores
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracao_setores", columnDefinition = "jsonb")
    private String configuracaoSetores;

    @Size(max = 5)
    @Column(name = "setor_oficina")
    private String setorOficina;

    @Size(max = 5)
    @Column(name = "setor_saida_rapida")
    private String setorSaidaRapida;
}