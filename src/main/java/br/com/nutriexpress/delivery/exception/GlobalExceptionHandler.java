package br.com.nutriexpress.delivery.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.nutriexpress.delivery.dto.ErroResponseDTO;

/**
 * Converte as exceções da aplicação em respostas HTTP padronizadas (ErroResponseDTO),
 * para que o cliente nunca receba um 500 por erro de entrada ou ID inexistente.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // 400: corpo da requisição reprovado no @Valid, com o mapa campo -> mensagem
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarErrosDeValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    // 400: parâmetro de URL reprovado na validação (ex.: /pratos/calorias?max=-1)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroInvalido(HandlerMethodValidationException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(resultado -> resultado.getResolvableErrors()
                .forEach(erro -> erros.putIfAbsent(
                        resultado.getMethodParameter().getParameterName(), erro.getDefaultMessage())));
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", erros);
    }

    // 400: parâmetro com tipo errado (ex.: /pratos/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue(), null);
    }

    // 400: parâmetro obrigatório ausente (ex.: /pratos/calorias sem ?max=)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroAusente(MissingServletRequestParameterException ex) {
        return resposta(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório", null);
    }

    // 400: JSON malformado ou com tipos incompatíveis
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado", null);
    }

    // 404: prato inexistente
    @ExceptionHandler(PratoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarPratoNaoEncontrado(PratoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 409: regra de negócio de nome único violada
    @ExceptionHandler(PratoJaCadastradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarPratoDuplicado(PratoJaCadastradoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // 409: restrição do banco violada (ex.: dois cadastros simultâneos com o mesmo nome
    // passando juntos pela checagem do Service e barrados pelo UNIQUE da coluna)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Operação viola uma restrição de dados (ex.: nome já cadastrado)", null);
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ErroResponseDTO(status.value(), mensagem, erros));
    }
}
