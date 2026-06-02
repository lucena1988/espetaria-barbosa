package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.FormaPagamento;
import br.com.espetariabarbosa.service.PagamentoService;
import br.com.espetariabarbosa.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PedidoService pedidoService;
    private final PagamentoService pagamentoService;

    @GetMapping("/pedido/{pedidoId}")
    public String fecharConta(@PathVariable Long pedidoId, Model model) {
        model.addAttribute("pedido", pedidoService.buscarPorId(pedidoId));
        model.addAttribute("formasPagamento", FormaPagamento.values());
        return "pagamentos/fechar-conta";
    }

    @PostMapping("/pedido/{pedidoId}")
    public String registrar(@PathVariable Long pedidoId,
                            @RequestParam List<FormaPagamento> formasPagamento,
                            @RequestParam List<BigDecimal> valoresPagamento,
                            @RequestParam(required = false) BigDecimal desconto,
                            @RequestParam(required = false) BigDecimal taxaServico) {
        pagamentoService.registrarPagamento(pedidoId, formasPagamento, valoresPagamento, desconto, taxaServico);
        return "redirect:/pedidos/historico";
    }
}
