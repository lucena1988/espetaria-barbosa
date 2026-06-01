package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.CategoriaProduto;
import br.com.espetariabarbosa.service.CategoriaProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categorias")
public class CategoriaProdutoController {

    private final CategoriaProdutoService categoriaProdutoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaProdutoService.listarTodas());
        model.addAttribute("categoriaProduto", new CategoriaProduto());
        return "categorias/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute CategoriaProduto categoriaProduto) {
        categoriaProdutoService.salvar(categoriaProduto);
        return "redirect:/categorias";
    }
}
