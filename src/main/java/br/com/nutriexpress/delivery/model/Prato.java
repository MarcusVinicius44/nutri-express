package br.com.nutriexpress.delivery.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade JPA que representa um prato do cardápio, mapeada para a tabela "pratos" no PostgreSQL.
 * Nunca é exposta diretamente pela API: o Controller trabalha apenas com DTOs.
 */
@Entity
@Table(name = "pratos")
@Getter
@Setter
@NoArgsConstructor
public class Prato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    // preço em reais: BigDecimal evita erros de arredondamento de ponto flutuante
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    // "vegano", "low carb", "fitness" ou "sobremesa saudável"
    @Column(nullable = false, length = 30)
    private String categoria;

    @Column(nullable = false)
    private Integer calorias;

    // porção numérica, em gramas ou mililitros (ver unidadeMedida)
    @Column(nullable = false)
    private Double quantidade;

    // "g" para gramas ou "ml" para mililitros
    @Column(name = "unidade_medida", nullable = false, length = 2)
    private String unidadeMedida;
}
