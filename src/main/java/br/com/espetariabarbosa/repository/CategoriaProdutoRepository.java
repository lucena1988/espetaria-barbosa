package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaProdutoRepository extends JpaRepository<CategoriaProduto, Long> {
    List<CategoriaProduto> findByAtivaTrueOrderByNomeAsc();
    List<CategoriaProduto> findByAtivaFalseOrderByNomeAsc();
    List<CategoriaProduto> findAllByOrderByNomeAsc();
}
