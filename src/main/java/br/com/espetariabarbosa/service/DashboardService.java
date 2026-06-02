package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pagamento;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.enums.FormaPagamento;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;

    public DashboardResumo gerarResumo(String tipoPeriodo, LocalDate dataFiltro, LocalDate dataInicioFiltro,
                                       LocalDate dataFimFiltro, YearMonth mesFiltro) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        boolean filtroMensal = "mes".equalsIgnoreCase(tipoPeriodo);
        boolean filtroIntervalo = "intervalo".equalsIgnoreCase(tipoPeriodo);
        LocalDate dataReferencia = dataFiltro == null ? LocalDate.now() : dataFiltro;
        LocalDate dataInicioReferencia = dataInicioFiltro == null ? dataReferencia : dataInicioFiltro;
        LocalDate dataFimReferencia = dataFimFiltro == null ? dataInicioReferencia : dataFimFiltro;
        if (dataFimReferencia.isBefore(dataInicioReferencia)) {
            LocalDate dataTemporaria = dataInicioReferencia;
            dataInicioReferencia = dataFimReferencia;
            dataFimReferencia = dataTemporaria;
        }
        LocalDate dataInicioPeriodo = dataInicioReferencia;
        LocalDate dataFimPeriodo = dataFimReferencia;
        YearMonth mesReferencia = mesFiltro == null ? YearMonth.from(dataReferencia) : mesFiltro;

        List<Pedido> pedidosDoPeriodo = pedidos.stream()
                .filter(pedido -> pedido.getCriadoEm() != null)
                .filter(pedido -> pertenceAoPeriodo(pedido, filtroMensal, filtroIntervalo, dataReferencia,
                        dataInicioPeriodo, dataFimPeriodo, mesReferencia))
                .filter(this::pedidoValidoParaFaturamento)
                .toList();

        BigDecimal vendasDoPeriodo = somarValorFaturamento(pedidosDoPeriodo);
        long pedidosAbertos = pedidos.stream()
                .filter(pedido -> List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
                        .contains(pedido.getStatus()))
                .count();

        BigDecimal ticketMedio = pedidosDoPeriodo.isEmpty()
                ? BigDecimal.ZERO
                : vendasDoPeriodo.divide(BigDecimal.valueOf(pedidosDoPeriodo.size()), 2, RoundingMode.HALF_UP);

        BigDecimal faturamentoMensal = somarValorFaturamento(pedidos.stream()
                .filter(pedido -> pedido.getCriadoEm() != null)
                .filter(pedido -> YearMonth.from(pedido.getCriadoEm()).equals(mesReferencia))
                .filter(this::pedidoValidoParaFaturamento)
                .toList());

        List<ProdutoMaisVendido> produtosMaisVendidos = pedidosDoPeriodo.stream()
                .flatMap(pedido -> pedido.getItens().stream())
                .collect(Collectors.groupingBy(
                        ItemPedido::getNomeProduto,
                        Collectors.summingInt(item -> item.getQuantidade() == null ? 0 : item.getQuantidade())
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> new ProdutoMaisVendido(entry.getKey(), entry.getValue()))
                .toList();

        return new DashboardResumo(
                filtroMensal ? "mes" : (filtroIntervalo ? "intervalo" : "dia"),
                dataReferencia.toString(),
                dataInicioPeriodo.toString(),
                dataFimPeriodo.toString(),
                mesReferencia.toString(),
                filtroMensal
                        ? mesReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                        : filtroIntervalo
                        ? dataInicioPeriodo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " a " + dataFimPeriodo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : dataReferencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                vendasDoPeriodo,
                pedidosDoPeriodo.size(),
                pedidosAbertos,
                ticketMedio,
                faturamentoMensal,
                produtosMaisVendidos
        );
    }

    public ExtratoFinanceiro gerarExtrato(String tipoPeriodo, LocalDate dataFiltro, LocalDate dataInicioFiltro,
                                          LocalDate dataFimFiltro, YearMonth mesFiltro) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        boolean filtroMensal = "mes".equalsIgnoreCase(tipoPeriodo);
        boolean filtroIntervalo = "intervalo".equalsIgnoreCase(tipoPeriodo);
        LocalDate dataReferencia = dataFiltro == null ? LocalDate.now() : dataFiltro;
        LocalDate dataInicioReferencia = dataInicioFiltro == null ? dataReferencia : dataInicioFiltro;
        LocalDate dataFimReferencia = dataFimFiltro == null ? dataInicioReferencia : dataFimFiltro;
        if (dataFimReferencia.isBefore(dataInicioReferencia)) {
            LocalDate dataTemporaria = dataInicioReferencia;
            dataInicioReferencia = dataFimReferencia;
            dataFimReferencia = dataTemporaria;
        }
        LocalDate dataInicioPeriodo = dataInicioReferencia;
        LocalDate dataFimPeriodo = dataFimReferencia;
        YearMonth mesReferencia = mesFiltro == null ? YearMonth.from(dataReferencia) : mesFiltro;

        List<Pedido> pedidosDoPeriodo = pedidos.stream()
                .filter(pedido -> pedido.getCriadoEm() != null)
                .filter(pedido -> pertenceAoPeriodo(pedido, filtroMensal, filtroIntervalo, dataReferencia,
                        dataInicioPeriodo, dataFimPeriodo, mesReferencia))
                .filter(this::pedidoValidoParaFaturamento)
                .sorted(Comparator.comparing(Pedido::getCriadoEm))
                .toList();

        BigDecimal totalRecebido = pedidosDoPeriodo.stream()
                .map(Pedido::getPagamento)
                .filter(pagamento -> pagamento != null && pagamento.getValor() != null)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendente = pedidosDoPeriodo.stream()
                .filter(pedido -> pedido.getPagamento() == null)
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> totaisPorForma = new LinkedHashMap<>();
        for (FormaPagamento forma : FormaPagamento.values()) {
            BigDecimal totalForma = pedidosDoPeriodo.stream()
                    .map(Pedido::getPagamento)
                    .filter(pagamento -> pagamento != null)
                    .map(pagamento -> valorPorForma(pagamento, forma))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totaisPorForma.put(forma.name(), totalForma);
        }

        return new ExtratoFinanceiro(
                gerarResumo(tipoPeriodo, dataFiltro, dataInicioFiltro, dataFimFiltro, mesFiltro),
                pedidosDoPeriodo,
                totalRecebido,
                totalPendente,
                totaisPorForma
        );
    }

    private boolean pertenceAoPeriodo(Pedido pedido, boolean filtroMensal, boolean filtroIntervalo,
                                      LocalDate dataReferencia, LocalDate dataInicioReferencia,
                                      LocalDate dataFimReferencia, YearMonth mesReferencia) {
        if (filtroMensal) {
            return YearMonth.from(pedido.getCriadoEm()).equals(mesReferencia);
        }

        LocalDate dataPedido = pedido.getCriadoEm().toLocalDate();
        if (filtroIntervalo) {
            return !dataPedido.isBefore(dataInicioReferencia) && !dataPedido.isAfter(dataFimReferencia);
        }

        return dataPedido.isEqual(dataReferencia);
    }

    private BigDecimal somarValorFaturamento(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(this::valorFaturamento)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal valorFaturamento(Pedido pedido) {
        if (pedido.getPagamento() != null && pedido.getPagamento().getValor() != null) {
            return pedido.getPagamento().getValor();
        }

        return pedido.getTotal();
    }

    private BigDecimal valorPorForma(Pagamento pagamento, FormaPagamento forma) {
        if (pagamento.getParcelas() != null && !pagamento.getParcelas().isEmpty()) {
            return pagamento.getParcelas().stream()
                    .filter(parcela -> parcela.getFormaPagamento() == forma)
                    .map(parcela -> parcela.getValor() == null ? BigDecimal.ZERO : parcela.getValor())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        if (pagamento.getFormaPagamento() != forma) {
            return BigDecimal.ZERO;
        }

        return pagamento.getValor() == null ? BigDecimal.ZERO : pagamento.getValor();
    }

    private boolean pedidoValidoParaFaturamento(Pedido pedido) {
        return pedido.getStatus() != StatusPedido.CANCELADO;
    }

    public record DashboardResumo(
            String tipoPeriodo,
            String dataFiltro,
            String dataInicioFiltro,
            String dataFimFiltro,
            String mesFiltro,
            String periodoLabel,
            BigDecimal vendasDoPeriodo,
            int pedidosDoPeriodo,
            long pedidosAbertos,
            BigDecimal ticketMedio,
            BigDecimal faturamentoMensal,
            List<ProdutoMaisVendido> produtosMaisVendidos
    ) {
    }

    public record ProdutoMaisVendido(String nome, Integer quantidade) {
    }

    public record ExtratoFinanceiro(
            DashboardResumo resumo,
            List<Pedido> pedidos,
            BigDecimal totalRecebido,
            BigDecimal totalPendente,
            Map<String, BigDecimal> totaisPorForma
    ) {
    }
}
