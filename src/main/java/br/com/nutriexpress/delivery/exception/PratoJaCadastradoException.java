package br.com.nutriexpress.delivery.exception;

/**
 * Violação da regra de negócio "não podem existir dois pratos com o mesmo nome".
 * Convertida em 409 Conflict pelo GlobalExceptionHandler.
 */
public class PratoJaCadastradoException extends RuntimeException {

    public PratoJaCadastradoException(String nome) {
        super("Já existe um prato cadastrado com o nome: " + nome);
    }
}
