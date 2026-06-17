package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.CategoriaProduto;
import br.com.espetariabarbosa.repository.CategoriaProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaProdutoService {

    private final CategoriaProdutoRepository categoriaProdutoRepository;

    public List<CategoriaProduto> listarAtivas() {
        return categoriaProdutoRepository.findByAtivaTrueOrderByNomeAsc();
    }

    public List<CategoriaProduto> listarInativas() {
        return categoriaProdutoRepository.findByAtivaFalseOrderByNomeAsc();
    }

    public List<CategoriaProduto> listarTodas() {
        return categoriaProdutoRepository.findAllByOrderByNomeAsc();
    }

    public CategoriaProduto salvar(CategoriaProduto categoriaProduto) {
        if (categoriaProduto.getAtiva() == null) {
            categoriaProduto.setAtiva(true);
        }
        return categoriaProdutoRepository.save(categoriaProduto);
    }

    public CategoriaProduto buscarPorId(Long id) {
        return categoriaProdutoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria nao encontrada"));
    }

    public void arquivar(Long id) {
        CategoriaProduto categoriaProduto = buscarPorId(id);
        categoriaProduto.setAtiva(false);
        categoriaProdutoRepository.save(categoriaProduto);
    }

    public void reativar(Long id) {
        CategoriaProduto categoriaProduto = buscarPorId(id);
        categoriaProduto.setAtiva(true);
        salvar(categoriaProduto);
    }
}
