package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Cliente;
import br.com.espetariabarbosa.service.ClienteService;
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
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarAtivos());
        model.addAttribute("clientesInativos", clienteService.listarInativos());
        model.addAttribute("cliente", new Cliente());
        return "clientes/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Cliente cliente) {
        clienteService.salvar(cliente);
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @RequestParam String nome,
                         @RequestParam(required = false) String telefone,
                         @RequestParam(required = false) String endereco,
                         RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorId(id);
            cliente.setNome(nome);
            cliente.setTelefone(telefone);
            cliente.setEndereco(endereco);
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/arquivar")
    public String arquivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.arquivar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Cliente excluido da tela com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.reativar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Cliente reativado com sucesso");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/clientes";
    }
}
