package br.com.espetariabarbosa.controller;

import br.com.espetariabarbosa.service.DashboardService;
import br.com.espetariabarbosa.service.ExtratoPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final ExtratoPdfService extratoPdfService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "dia") String periodo,
                            @RequestParam(required = false) String data,
                            @RequestParam(required = false) String dataInicio,
                            @RequestParam(required = false) String dataFim,
                            @RequestParam(required = false) String mes,
                            Model model) {
        LocalDate dataFiltro = parseData(data);
        LocalDate dataInicioFiltro = parseData(dataInicio);
        LocalDate dataFimFiltro = parseData(dataFim);
        YearMonth mesFiltro = parseMes(mes);

        model.addAttribute("resumo", dashboardService.gerarResumo(periodo, dataFiltro, dataInicioFiltro, dataFimFiltro, mesFiltro));
        return "dashboard/index";
    }

    @GetMapping("/dashboard/extrato.pdf")
    public ResponseEntity<byte[]> extratoPdf(@RequestParam(required = false, defaultValue = "dia") String periodo,
                                             @RequestParam(required = false) String data,
                                             @RequestParam(required = false) String dataInicio,
                                             @RequestParam(required = false) String dataFim,
                                             @RequestParam(required = false) String mes) {
        LocalDate dataFiltro = parseData(data);
        LocalDate dataInicioFiltro = parseData(dataInicio);
        LocalDate dataFimFiltro = parseData(dataFim);
        YearMonth mesFiltro = parseMes(mes);
        var extrato = dashboardService.gerarExtrato(periodo, dataFiltro, dataInicioFiltro, dataFimFiltro, mesFiltro);
        byte[] pdf = extratoPdfService.gerar(extrato);

        String nomeArquivo = "extrato-financeiro-" + extrato.resumo().tipoPeriodo() + "-"
                + ("mes".equals(extrato.resumo().tipoPeriodo())
                ? extrato.resumo().mesFiltro()
                : "intervalo".equals(extrato.resumo().tipoPeriodo())
                ? extrato.resumo().dataInicioFiltro() + "-a-" + extrato.resumo().dataFimFiltro()
                : extrato.resumo().dataFiltro())
                + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(nomeArquivo)
                        .build()
                        .toString())
                .body(pdf);
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
