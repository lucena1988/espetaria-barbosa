package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.service.ClienteService;
import br.com.espetariabarbosa.service.MesaService;
import br.com.espetariabarbosa.service.PedidoService;
import br.com.espetariabarbosa.service.ProdutoService;
import br.com.espetariabarbosa.service.ReciboPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final ReciboPdfService reciboPdfService;

    @GetMapping
    public String listar(Model model) {
        var pedidos = pedidoService.listarAtivos();
        model.addAttribute("pedidos", pedidos);
        model.addAttribute("pedidosUnificaveis", pedidos);
        model.addAttribute("mesas", mesaService.listarDisponiveisParaPedido());
        model.addAttribute("produtos", produtoService.listarAtivos());
        return "pedidos/lista";
    }

    @GetMapping("/historico")
    public String historico(Model model) {
        model.addAttribute("pedidos", pedidoService.listarHistorico());
        return "pedidos/historico";
    }

    @GetMapping("/{id}/recibo.pdf")
    public ResponseEntity<byte[]> recibo(@PathVariable Long id) {
        var pedido = pedidoService.buscarPorId(id);
        byte[] pdf = reciboPdfService.gerar(pedido);
        String filename = "recibo-pedido-" + id + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename)
                        .build()
                        .toString())
                .body(pdf);
    }

    @PostMapping("/{id}/ocultar-historico")
    public String ocultarHistorico(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.ocultarDoHistorico(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido #" + id + " ocultado do historico");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/pedidos/historico";
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

    @PostMapping("/{id}/transferir-mesa")
    public String transferirMesa(@PathVariable Long id,
                                 @RequestParam String novaMesa,
                                 RedirectAttributes redirectAttributes) {
        try {
            pedidoService.transferirMesa(id, novaMesa);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido transferido para " + novaMesa);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Long id,
                                @RequestParam Long produtoId,
                                @RequestParam Integer quantidade,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoService.adicionarItem(id, produtoId, quantidade);
            redirectAttributes.addFlashAttribute("sucesso", "Item adicionado ao pedido #" + id);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/pedidos";
    }

    @PostMapping("/{pedidoId}/itens/{itemId}/remover")
    public String removerItem(@PathVariable Long pedidoId,
                              @PathVariable Long itemId,
                              RedirectAttributes redirectAttributes) {
        try {
            pedidoService.removerItem(pedidoId, itemId);
            redirectAttributes.addFlashAttribute("sucesso", "Item removido do pedido #" + pedidoId);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/pedidos";
    }

    @PostMapping("/{id}/juntar")
    public String juntarPedidos(@PathVariable Long id,
                                @RequestParam Long pedidoDestinoId,
                                RedirectAttributes redirectAttributes) {
        try {
            pedidoService.juntarPedidos(id, pedidoDestinoId);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido #" + id + " juntado ao pedido #" + pedidoDestinoId);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }

        return "redirect:/pedidos";
    }
}
