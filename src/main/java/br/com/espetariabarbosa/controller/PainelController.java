package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PainelController {

    private final PedidoService pedidoService;

    @GetMapping("/painel")
    public String painel(Model model) {
        model.addAttribute("pedidos", pedidoService.listarPainel());
        return "painel/index";
    }
}
