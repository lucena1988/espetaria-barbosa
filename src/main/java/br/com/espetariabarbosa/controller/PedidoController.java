package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.service.MesaService;
import br.com.espetariabarbosa.service.PedidoService;
import br.com.espetariabarbosa.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final MesaService mesaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        return "pedidos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("mesas", mesaService.listarDisponiveisParaPedido());
        model.addAttribute("tiposAtendimento", TipoAtendimento.values());
        return "pedidos/novo";
    }

    @PostMapping
    public String criar(@RequestParam String nomeCliente,
                        @RequestParam String mesa,
                        @RequestParam TipoAtendimento tipoAtendimento,
                        @RequestParam List<Long> produtoIds,
                        @RequestParam List<Integer> quantidades) {
        pedidoService.criarPedido(nomeCliente, mesa, tipoAtendimento, produtoIds, quantidades);
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.alterarStatus(id, status);
        return "redirect:/pedidos";
    }
}
