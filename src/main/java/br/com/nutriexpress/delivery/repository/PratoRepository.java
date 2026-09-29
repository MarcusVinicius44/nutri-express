package br.com.nutriexpress.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.nutriexpress.delivery.model.Prato;

/**
 * Acesso ao banco para a entidade Prato. O Spring Data JPA gera a implementação
 * de todos os métodos a partir dos nomes, sem SQL escrito à mão.
 */
public interface PratoRepository extends JpaRepository<Prato, Long> {

    List<Prato> findByCategoria(String categoria);

    List<Prato> findByCaloriasLessThanEqualOrderByCaloriasAsc(Integer calorias);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
