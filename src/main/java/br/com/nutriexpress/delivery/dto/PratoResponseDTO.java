package br.com.nutriexpress.delivery.dto;

import java.math.BigDecimal;

/**
 * Dados de um prato devolvidos ao cliente.
 *
 * Escolha do grupo: todos os campos da entidade são expostos. Em um app de comida
 * saudável, as informações nutricionais (calorias, quantidade e unidade de medida)
 * são justamente o que o cliente usa para escolher o prato, e nenhum campo de Prato
 * é interno ou sensível. Separar o DTO da entidade ainda garante que mudanças
 * futuras na tabela (ex.: campos de auditoria) não vazem automaticamente na API.
 */
public record PratoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        String categoria,
        Integer calorias,
        Double quantidade,
        String unidadeMedida) {
}
