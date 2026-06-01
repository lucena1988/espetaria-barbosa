package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Mesa;
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
@RequestMapping("/cardapio")
public class CardapioController {

    private final MesaService mesaService;
    private final ProdutoService produtoService;
    private final PedidoService pedidoService;

    @GetMapping("/mesa/{mesaId}")
    public String cardapioMesa(@PathVariable Long mesaId,
                               @RequestParam(required = false) Boolean sucesso,
                               Model model) {
        Mesa mesa = mesaService.buscarPorId(mesaId);
        model.addAttribute("mesa", mesa);
        model.addAttribute("produtos", produtoService.listarAtivos());
        model.addAttribute("sucesso", Boolean.TRUE.equals(sucesso));
        return "cardapio/mesa";
    }

    @PostMapping("/mesa/{mesaId}/pedidos")
    public String criarPedidoMesa(@PathVariable Long mesaId,
                                  @RequestParam String nomeCliente,
                                  @RequestParam List<Long> produtoIds,
                                  @RequestParam List<Integer> quantidades) {
        Mesa mesa = mesaService.buscarPorId(mesaId);
        pedidoService.criarPedido(nomeCliente, mesa.getNumero(), TipoAtendimento.MESA, produtoIds, quantidades);
        return "redirect:/cardapio/mesa/" + mesaId + "?sucesso=true";
    }
}
