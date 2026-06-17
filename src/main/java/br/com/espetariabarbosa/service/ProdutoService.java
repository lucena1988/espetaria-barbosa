package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<Produto> listarAtivos() {
        return produtoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public List<Produto> listarInativos() {
        return produtoRepository.findByAtivoFalseOrderByNomeAsc();
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto salvar(Produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        if (produto.getQuantidadeEstoque() == null) {
            produto.setQuantidadeEstoque(0);
        }
        if (produto.getEstoqueMinimo() == null) {
            produto.setEstoqueMinimo(5);
        }
        if (produto.getCategoriaProduto() != null) {
            produto.setCategoria(produto.getCategoriaProduto().getNome());
        }
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto nao encontrado"));
    }

    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    public void reativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(true);
        salvar(produto);
    }

    public void baixarEstoque(Produto produto, Integer quantidade) {
        if (!produto.possuiEstoque(quantidade)) {
            throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
        }

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        produtoRepository.save(produto);
    }

    public void reporEstoque(Long produtoId, String nomeProduto, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            return;
        }

        Produto produto = null;
        if (produtoId != null) {
            produto = buscarPorId(produtoId);
        } else if (nomeProduto != null && !nomeProduto.isBlank()) {
            produto = produtoRepository.findFirstByNome(nomeProduto).orElse(null);
        }

        if (produto == null) {
            return;
        }

        int estoqueAtual = produto.getQuantidadeEstoque() == null ? 0 : produto.getQuantidadeEstoque();
        produto.setQuantidadeEstoque(estoqueAtual + quantidade);
        produtoRepository.save(produto);
    }
}
