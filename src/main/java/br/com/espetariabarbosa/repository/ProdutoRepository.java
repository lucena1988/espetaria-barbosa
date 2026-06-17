package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByAtivoTrueOrderByNomeAsc();
    List<Produto> findByAtivoFalseOrderByNomeAsc();
    Optional<Produto> findFirstByNome(String nome);
}
