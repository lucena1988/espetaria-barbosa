package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.service.CategoriaProdutoService;
import br.com.espetariabarbosa.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CategoriaProdutoService categoriaProdutoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("produtosInativos", produtoService.listarInativos());
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

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nome,
                         @RequestParam Long categoriaId,
                         @RequestParam BigDecimal preco,
                         @RequestParam Integer quantidadeEstoque,
                         @RequestParam Integer estoqueMinimo,
                         RedirectAttributes redirectAttributes) {
        try {
            Produto produto = produtoService.buscarPorId(id);
            var categoria = categoriaProdutoService.buscarPorId(categoriaId);
            produto.setNome(nome);
            produto.setCategoriaProduto(categoria);
            produto.setCategoria(categoria.getNome());
            produto.setPreco(preco);
            produto.setQuantidadeEstoque(quantidadeEstoque);
            produto.setEstoqueMinimo(estoqueMinimo);
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto atualizado com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/produtos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Produto excluido da tela com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/produtos";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produtoService.reativar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Produto reativado com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
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
