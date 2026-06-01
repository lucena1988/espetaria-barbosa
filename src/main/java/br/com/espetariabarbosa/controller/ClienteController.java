package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Cliente;
import br.com.espetariabarbosa.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("cliente", new Cliente());
        return "clientes/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Cliente cliente) {
        clienteService.salvar(cliente);
        return "redirect:/clientes";
    }
}
