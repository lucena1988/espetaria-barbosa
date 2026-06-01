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

    public DashboardResumo gerarResumo(String tipoPeriodo, LocalDate dataFiltro, YearMonth mesFiltro) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        boolean filtroMensal = "mes".equalsIgnoreCase(tipoPeriodo);
        LocalDate dataReferencia = dataFiltro == null ? LocalDate.now() : dataFiltro;
        YearMonth mesReferencia = mesFiltro == null ? YearMonth.from(dataReferencia) : mesFiltro;

        List<Pedido> pedidosDoPeriodo = pedidos.stream()
                .filter(pedido -> pedido.getCriadoEm() != null)
                .filter(pedido -> pertenceAoPeriodo(pedido, filtroMensal, dataReferencia, mesReferencia))
                .filter(this::pedidoValidoParaFaturamento)
                .toList();

        BigDecimal vendasDoPeriodo = somarTotal(pedidosDoPeriodo);
        long pedidosAbertos = pedidos.stream()
                .filter(pedido -> List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
                        .contains(pedido.getStatus()))
                .count();

        BigDecimal ticketMedio = pedidosDoPeriodo.isEmpty()
                ? BigDecimal.ZERO
                : vendasDoPeriodo.divide(BigDecimal.valueOf(pedidosDoPeriodo.size()), 2, RoundingMode.HALF_UP);

        BigDecimal faturamentoMensal = somarTotal(pedidos.stream()
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
                filtroMensal ? "mes" : "dia",
                dataReferencia.toString(),
                mesReferencia.toString(),
                filtroMensal
                        ? mesReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy"))
                        : dataReferencia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                vendasDoPeriodo,
                pedidosDoPeriodo.size(),
                pedidosAbertos,
                ticketMedio,
                faturamentoMensal,
                produtosMaisVendidos
        );
    }

    public ExtratoFinanceiro gerarExtrato(String tipoPeriodo, LocalDate dataFiltro, YearMonth mesFiltro) {
        List<Pedido> pedidos = pedidoRepository.findAll();
        boolean filtroMensal = "mes".equalsIgnoreCase(tipoPeriodo);
        LocalDate dataReferencia = dataFiltro == null ? LocalDate.now() : dataFiltro;
        YearMonth mesReferencia = mesFiltro == null ? YearMonth.from(dataReferencia) : mesFiltro;

        List<Pedido> pedidosDoPeriodo = pedidos.stream()
                .filter(pedido -> pedido.getCriadoEm() != null)
                .filter(pedido -> pertenceAoPeriodo(pedido, filtroMensal, dataReferencia, mesReferencia))
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
                    .filter(pagamento -> pagamento != null && pagamento.getFormaPagamento() == forma)
                    .map(Pagamento::getValor)
                    .filter(valor -> valor != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totaisPorForma.put(forma.name(), totalForma);
        }

        return new ExtratoFinanceiro(
                gerarResumo(tipoPeriodo, dataFiltro, mesFiltro),
                pedidosDoPeriodo,
                totalRecebido,
                totalPendente,
                totaisPorForma
        );
    }

    private boolean pertenceAoPeriodo(Pedido pedido, boolean filtroMensal, LocalDate dataReferencia, YearMonth mesReferencia) {
        if (filtroMensal) {
            return YearMonth.from(pedido.getCriadoEm()).equals(mesReferencia);
        }

        return pedido.getCriadoEm().toLocalDate().isEqual(dataReferencia);
    }

    private BigDecimal somarTotal(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(Pedido::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean pedidoValidoParaFaturamento(Pedido pedido) {
        return pedido.getStatus() != StatusPedido.CANCELADO;
    }

    public record DashboardResumo(
            String tipoPeriodo,
            String dataFiltro,
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
