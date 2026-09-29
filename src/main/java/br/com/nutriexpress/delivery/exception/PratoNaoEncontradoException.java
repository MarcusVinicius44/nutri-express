package br.com.nutriexpress.delivery.exception;

/**
 * Lançada quando não existe prato com o ID informado.
 * Convertida em 404 Not Found pelo GlobalExceptionHandler.
 */
public class PratoNaoEncontradoException extends RuntimeException {

    public PratoNaoEncontradoException(Long id) {
        super("Prato não encontrado com o ID: " + id);
    }
}
