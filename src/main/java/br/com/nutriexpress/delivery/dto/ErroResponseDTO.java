package br.com.nutriexpress.delivery.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato padrão das respostas de erro da API.
 * "erros" só aparece em falhas de validação, com a mensagem de cada campo inválido.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponseDTO(
        int status,
        String mensagem,
        Map<String, String> erros,
        LocalDateTime timestamp) {

    public ErroResponseDTO(int status, String mensagem) {
        this(status, mensagem, null, LocalDateTime.now());
    }

    public ErroResponseDTO(int status, String mensagem, Map<String, String> erros) {
        this(status, mensagem, erros, LocalDateTime.now());
    }
}
