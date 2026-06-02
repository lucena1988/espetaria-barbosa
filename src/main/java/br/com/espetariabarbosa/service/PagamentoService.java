package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Pagamento;
import br.com.espetariabarbosa.entity.ParcelaPagamento;
import br.com.espetariabarbosa.enums.FormaPagamento;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoService pedidoService;

    public Pagamento registrarPagamento(Long pedidoId, List<FormaPagamento> formasPagamento,
                                        List<BigDecimal> valoresPagamento, BigDecimal desconto,
                                        BigDecimal taxaServico) {
        if (pagamentoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalArgumentException("Pedido ja possui pagamento registrado");
        }

        var pedido = pedidoService.buscarPorId(pedidoId);
        BigDecimal valorOriginal = normalizar(pedido.getTotal());
        BigDecimal valorDesconto = normalizar(desconto);
        BigDecimal valorTaxaServico = normalizar(taxaServico);

        if (valorDesconto.compareTo(BigDecimal.ZERO) < 0 || valorTaxaServico.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Desconto e taxa de servico nao podem ser negativos");
        }

        BigDecimal valorPagamento = valorOriginal
                .subtract(valorDesconto)
                .add(valorTaxaServico)
                .setScale(2, RoundingMode.HALF_UP);

        if (valorPagamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O desconto nao pode ser maior que o total com taxa");
        }

        List<ParcelaPagamento> parcelas = montarParcelas(formasPagamento, valoresPagamento);
        BigDecimal totalParcelas = parcelas.stream()
                .map(ParcelaPagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (totalParcelas.compareTo(valorPagamento) != 0) {
            throw new IllegalArgumentException("A soma dos pagamentos deve fechar exatamente o total a pagar");
        }

        Pagamento pagamento = Pagamento.builder()
                .pedido(pedido)
                .formaPagamento(parcelas.get(0).getFormaPagamento())
                .valorOriginal(valorOriginal)
                .desconto(valorDesconto)
                .taxaServico(valorTaxaServico)
                .valor(valorPagamento)
                .pagoEm(LocalDateTime.now())
                .build();
        parcelas.forEach(pagamento::adicionarParcela);

        pedido.setPagamento(pagamento);
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedidoService.salvar(pedido);
        return pagamento;
    }

    private List<ParcelaPagamento> montarParcelas(List<FormaPagamento> formasPagamento, List<BigDecimal> valoresPagamento) {
        if (formasPagamento == null || valoresPagamento == null || formasPagamento.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma forma de pagamento");
        }

        List<ParcelaPagamento> parcelas = new java.util.ArrayList<>();
        for (int i = 0; i < formasPagamento.size(); i++) {
            FormaPagamento forma = formasPagamento.get(i);
            BigDecimal valor = i < valoresPagamento.size() ? normalizar(valoresPagamento.get(i)) : BigDecimal.ZERO;

            if (forma == null && valor.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (forma == null) {
                throw new IllegalArgumentException("Selecione a forma de pagamento de todas as parcelas");
            }
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("O valor de cada pagamento deve ser maior que zero");
            }

            parcelas.add(ParcelaPagamento.builder()
                    .formaPagamento(forma)
                    .valor(valor)
                    .build());
        }

        if (parcelas.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma forma de pagamento");
        }

        return parcelas;
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}
