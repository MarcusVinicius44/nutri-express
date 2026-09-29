package br.com.nutriexpress.delivery.service;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.nutriexpress.delivery.dto.PratoRequestDTO;
import br.com.nutriexpress.delivery.dto.PratoResponseDTO;
import br.com.nutriexpress.delivery.dto.PratoValorRequestDTO;
import br.com.nutriexpress.delivery.exception.PratoJaCadastradoException;
import br.com.nutriexpress.delivery.exception.PratoNaoEncontradoException;
import br.com.nutriexpress.delivery.model.Prato;
import br.com.nutriexpress.delivery.repository.PratoRepository;

/**
 * Regras de negócio e conversões do recurso Prato.
 * Todos os métodos públicos recebem e devolvem apenas DTOs; a entidade Prato
 * nunca sai desta camada.
 *
 * REGRA DE NEGÓCIO: não podem existir dois pratos com o mesmo nome.
 * A comparação ignora maiúsculas/minúsculas e espaços nas pontas, então
 * "Salada Caesar" e "  salada caesar " são considerados o mesmo prato.
 * Motivo: no cardápio do app o nome identifica o prato para o cliente; dois
 * pratos homônimos com preços ou calorias diferentes gerariam pedidos errados.
 * Vale na criação (POST) e na atualização (PUT), onde o próprio prato que está
 * sendo atualizado pode manter o nome atual. Violações geram 409 Conflict.
 */
@Service
@Transactional(readOnly = true)
public class PratoService {

    private final PratoRepository repository;

    public PratoService(PratoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PratoResponseDTO criar(PratoRequestDTO dto) {
        String nome = dto.nome().trim();
        // regra de negócio: nome único (ver javadoc da classe)
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new PratoJaCadastradoException(nome);
        }
        return toDTO(repository.save(toEntity(dto)));
    }

    public List<PratoResponseDTO> listarTodos() {
        return toDTOList(repository.findAll(Sort.by("id")));
    }

    public PratoResponseDTO buscarPorId(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public List<PratoResponseDTO> listarPorCategoria(String categoria) {
        return toDTOList(repository.findByCategoria(normalizar(categoria)));
    }

    public List<PratoResponseDTO> listarPorCaloriasMaximas(Integer max) {
        return toDTOList(repository.findByCaloriasLessThanEqualOrderByCaloriasAsc(max));
    }

    @Transactional
    public PratoResponseDTO atualizar(Long id, PratoRequestDTO dto) {
        Prato prato = buscarEntidade(id);
        String nome = dto.nome().trim();
        // regra de negócio: nome único, desconsiderando o próprio prato
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new PratoJaCadastradoException(nome);
        }
        copiarDados(dto, prato);
        return toDTO(repository.save(prato));
    }

    @Transactional
    public PratoResponseDTO atualizarValor(Long id, PratoValorRequestDTO dto) {
        Prato prato = buscarEntidade(id);
        prato.setValor(dto.valor());
        return toDTO(repository.save(prato));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new PratoNaoEncontradoException(id);
        }
        repository.deleteById(id);
    }

    private Prato buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new PratoNaoEncontradoException(id));
    }

    private Prato toEntity(PratoRequestDTO dto) {
        Prato prato = new Prato();
        copiarDados(dto, prato);
        return prato;
    }

    private PratoResponseDTO toDTO(Prato prato) {
        return new PratoResponseDTO(
                prato.getId(),
                prato.getNome(),
                prato.getDescricao(),
                prato.getValor(),
                prato.getCategoria(),
                prato.getCalorias(),
                prato.getQuantidade(),
                prato.getUnidadeMedida());
    }

    private List<PratoResponseDTO> toDTOList(List<Prato> pratos) {
        return pratos.stream().map(this::toDTO).toList();
    }

    // usado na criação e na atualização, para que as duas gravem os dados do mesmo jeito
    private void copiarDados(PratoRequestDTO dto, Prato prato) {
        prato.setNome(dto.nome().trim());
        prato.setDescricao(dto.descricao() == null ? null : dto.descricao().trim());
        prato.setValor(dto.valor());
        prato.setCategoria(normalizar(dto.categoria()));
        prato.setCalorias(dto.calorias());
        prato.setQuantidade(dto.quantidade());
        prato.setUnidadeMedida(normalizar(dto.unidadeMedida()));
    }

    // categorias e unidades são gravadas em minúsculas para que "Vegano" e "vegano" sejam iguais no filtro
    private String normalizar(String texto) {
        return texto.trim().toLowerCase(Locale.ROOT);
    }
}
