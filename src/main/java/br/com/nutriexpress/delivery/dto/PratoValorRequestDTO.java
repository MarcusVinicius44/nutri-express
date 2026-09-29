package br.com.nutriexpress.delivery.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Corpo do PATCH /pratos/{id}/valor: altera somente o preço do prato.
 */
public record PratoValorRequestDTO(

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor) {
}
