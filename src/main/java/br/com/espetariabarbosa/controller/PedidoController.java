package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.service.ClienteService;
import br.com.espetariabarbosa.service.MesaService;
import br.com.espetariabarbosa.service.PedidoService;
import br.com.espetariabarbosa.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProdutoService produtoService;
    private final MesaService mesaService;
    private final ClienteService clienteService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pedidos", pedidoService.listarAtivos());
        return "pedidos/lista";
    }

    @GetMapping("/historico")
    public String historico(Model model) {
        model.addAttribute("pedidos", pedidoService.listarHistorico());
        return "pedidos/historico";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) String erro, Model model) {
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("mesas", mesaService.listarDisponiveisParaPedido());
        model.addAttribute("clientes", clienteService.listarAtivos());
        model.addAttribute("tiposAtendimento", TipoAtendimento.values());
        model.addAttribute("erro", erro);
        return "pedidos/novo";
    }

    @PostMapping
    public String criar(@RequestParam(required = false) Long clienteId,
                        @RequestParam(required = false) String nomeCliente,
                        @RequestParam String mesa,
                        @RequestParam TipoAtendimento tipoAtendimento,
                        @RequestParam List<Long> produtoIds,
                        @RequestParam List<Integer> quantidades,
                        RedirectAttributes redirectAttributes) {
        try {
            pedidoService.criarPedido(clienteId, nomeCliente, mesa, tipoAtendimento, produtoIds, quantidades);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addAttribute("erro", exception.getMessage());
            return "redirect:/pedidos/novo";
        }
        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.alterarStatus(id, status);
        return "redirect:/pedidos";
    }
}
