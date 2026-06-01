package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.service.CategoriaProdutoService;
import br.com.espetariabarbosa.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CategoriaProdutoService categoriaProdutoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("categorias", categoriaProdutoService.listarAtivas());
        model.addAttribute("produto", new Produto());
        return "produtos/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Produto produto,
                         @RequestParam(required = false) Long categoriaId) {
        if (categoriaId != null) {
            var categoria = categoriaProdutoService.buscarPorId(categoriaId);
            produto.setCategoriaProduto(categoria);
            produto.setCategoria(categoria.getNome());
        }
        produtoService.salvar(produto);
        return "redirect:/produtos";
    }

    @PostMapping("/{id}/estoque")
    public String ajustarEstoque(@PathVariable Long id, @RequestParam Integer quantidadeEstoque) {
        Produto produto = produtoService.buscarPorId(id);
        produto.setQuantidadeEstoque(quantidadeEstoque);
        produtoService.salvar(produto);
        return "redirect:/produtos";
    }
}
