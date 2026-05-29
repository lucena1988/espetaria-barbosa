package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.entity.Mesa;
import br.com.espetariabarbosa.enums.StatusMesa;
import br.com.espetariabarbosa.service.MesaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mesas")
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("mesas", mesaService.listarAtivas());
        model.addAttribute("mesa", new Mesa());
        model.addAttribute("statusMesas", StatusMesa.values());
        return "mesas/lista";
    }

    @PostMapping
    public String salvar(@ModelAttribute Mesa mesa) {
        mesaService.salvar(mesa);
        return "redirect:/mesas";
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusMesa status) {
        mesaService.alterarStatus(id, status);
        return "redirect:/mesas";
    }
}
