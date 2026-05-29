package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.service.PedidoService;
import br.com.espetariabarbosa.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produtos", produtoService.listarAtivos());
        return "pedidos/novo";
    }

    @PostMapping
    public String criar(@RequestParam String nomeCliente,
                        @RequestParam String mesa,
                        @RequestParam Long produtoId,
                        @RequestParam Integer quantidade) {
        pedidoService.criarPedido(nomeCliente, mesa, produtoId, quantidade);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.alterarStatus(id, status);
        return "redirect:/pedidos";
    }
}
