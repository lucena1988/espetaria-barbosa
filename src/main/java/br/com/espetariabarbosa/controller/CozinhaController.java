package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cozinha")
public class CozinhaController {

    private final PedidoService pedidoService;

    @GetMapping
    public String cozinha(Model model) {
        var pedidos = pedidoService.listarPainel();
        model.addAttribute("pedidosRecebidos", pedidos.stream()
                .filter(pedido -> pedido.getStatus() == StatusPedido.RECEBIDO)
                .toList());
        model.addAttribute("pedidosEmPreparo", pedidos.stream()
                .filter(pedido -> pedido.getStatus() == StatusPedido.EM_PREPARO)
                .toList());
        model.addAttribute("pedidosProntos", pedidos.stream()
                .filter(pedido -> pedido.getStatus() == StatusPedido.PRONTO)
                .toList());
        return "cozinha/index";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusPedido status) {
        pedidoService.alterarStatus(id, status);
        return "redirect:/cozinha";
    }
}
