package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.CategoriaProduto;
import br.com.espetariabarbosa.service.CategoriaProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categorias")
public class CategoriaProdutoController {

    private final CategoriaProdutoService categoriaProdutoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaProdutoService.listarAtivas());
        model.addAttribute("categoriasInativas", categoriaProdutoService.listarInativas());
        model.addAttribute("categoriaProduto", new CategoriaProduto());
        return "categorias/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute CategoriaProduto categoriaProduto) {
        categoriaProdutoService.salvar(categoriaProduto);
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nome,
                         @RequestParam(required = false) String descricao,
                         RedirectAttributes redirectAttributes) {
        try {
            CategoriaProduto categoriaProduto = categoriaProdutoService.buscarPorId(id);
            categoriaProduto.setNome(nome);
            categoriaProduto.setDescricao(descricao);
            categoriaProdutoService.salvar(categoriaProduto);
            redirectAttributes.addFlashAttribute("sucesso", "Categoria atualizada com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/arquivar")
    public String arquivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaProdutoService.arquivar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Categoria excluida da tela com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoriaProdutoService.reativar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Categoria reativada com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/categorias";
    }
}
