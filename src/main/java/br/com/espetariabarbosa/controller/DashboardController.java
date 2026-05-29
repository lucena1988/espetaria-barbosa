package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "dia") String periodo,
                            @RequestParam(required = false) String data,
                            @RequestParam(required = false) String mes,
                            Model model) {
        LocalDate dataFiltro = parseData(data);
        YearMonth mesFiltro = parseMes(mes);

        model.addAttribute("resumo", dashboardService.gerarResumo(periodo, dataFiltro, mesFiltro));
        return "dashboard/index";
    }

    private LocalDate parseData(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        return LocalDate.parse(data);
    }

    private YearMonth parseMes(String mes) {
        if (mes == null || mes.isBlank()) {
            return null;
        }

        return YearMonth.parse(mes);
    }
}
