package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Funcionario;
import br.com.espetariabarbosa.enums.PerfilFuncionario;
import br.com.espetariabarbosa.service.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("funcionarios", funcionarioService.listarTodos());
        model.addAttribute("funcionario", new Funcionario());
        model.addAttribute("perfis", PerfilFuncionario.values());
        return "funcionarios/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Funcionario funcionario, RedirectAttributes redirectAttributes) {
        try {
            funcionarioService.salvar(funcionario);
            redirectAttributes.addAttribute("sucesso", "Funcionario cadastrado com sucesso");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addAttribute("erro", exception.getMessage());
        }
        return "redirect:/funcionarios";
    }
}
