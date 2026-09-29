package br.com.nutriexpress.delivery.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.nutriexpress.delivery.dto.PratoRequestDTO;
import br.com.nutriexpress.delivery.dto.PratoResponseDTO;
import br.com.nutriexpress.delivery.dto.PratoValorRequestDTO;
import br.com.nutriexpress.delivery.service.PratoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Endpoints REST do recurso Prato. Apenas recebe a requisição, valida com @Valid,
 * delega ao PratoService e devolve o status HTTP adequado. Erros (404, 400, 409)
 * são convertidos em resposta pelo GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/pratos")
public class PratoController {

    private final PratoService service;

    public PratoController(PratoService service) {
        this.service = service;
    }

    // GET /pratos -> 200
    @GetMapping
    public ResponseEntity<List<PratoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // GET /pratos?categoria=vegano -> 200 (só é escolhido quando o parâmetro está presente)
    @GetMapping(params = "categoria")
    public ResponseEntity<List<PratoResponseDTO>> listarPorCategoria(@RequestParam String categoria) {
        return ResponseEntity.ok(service.listarPorCategoria(categoria));
    }

    // GET /pratos/calorias?max=500 -> 200
    @GetMapping("/calorias")
    public ResponseEntity<List<PratoResponseDTO>> listarPorCaloriasMaximas(
            @RequestParam @PositiveOrZero(message = "O máximo de calorias não pode ser negativo") Integer max) {
        return ResponseEntity.ok(service.listarPorCaloriasMaximas(max));
    }

    // GET /pratos/{id} -> 200 ou 404
    @GetMapping("/{id}")
    public ResponseEntity<PratoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    // POST /pratos -> 201 com o prato criado e o header Location apontando para ele
    @PostMapping
    public ResponseEntity<PratoResponseDTO> criar(@Valid @RequestBody PratoRequestDTO dto) {
        PratoResponseDTO criado = service.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    // PUT /pratos/{id} -> 200 ou 404
    @PutMapping("/{id}")
    public ResponseEntity<PratoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody PratoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    // PATCH /pratos/{id}/valor -> 200 ou 404 (altera somente o valor)
    @PatchMapping("/{id}/valor")
    public ResponseEntity<PratoResponseDTO> atualizarValor(@PathVariable Long id,
            @Valid @RequestBody PratoValorRequestDTO dto) {
        return ResponseEntity.ok(service.atualizarValor(id, dto));
    }

    // DELETE /pratos/{id} -> 204 ou 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
