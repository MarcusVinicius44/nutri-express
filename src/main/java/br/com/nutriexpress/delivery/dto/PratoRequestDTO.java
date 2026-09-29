package br.com.nutriexpress.delivery.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Dados aceitos na criação (POST) e na atualização completa (PUT) de um prato.
 * Não possui id: o identificador é gerado pelo banco ou vem da URL.
 */
public record PratoRequestDTO(

        @NotBlank(message = "O nome do prato é obrigatório")
        @Size(max = 100, message = "O nome do prato deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @NotBlank(message = "A categoria é obrigatória")
        @Pattern(regexp = "(?iu)\\s*(vegano|low carb|fitness|sobremesa saudável)\\s*",
                message = "A categoria deve ser: vegano, low carb, fitness ou sobremesa saudável")
        String categoria,

        @NotNull(message = "As calorias são obrigatórias")
        @PositiveOrZero(message = "As calorias não podem ser negativas")
        Integer calorias,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Double quantidade,

        @NotBlank(message = "A unidade de medida é obrigatória")
        @Pattern(regexp = "(?i)\\s*(g|ml)\\s*", message = "A unidade de medida deve ser \"g\" ou \"ml\"")
        String unidadeMedida) {
}
