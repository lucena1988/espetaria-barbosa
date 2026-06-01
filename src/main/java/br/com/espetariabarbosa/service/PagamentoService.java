package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Pagamento;
import br.com.espetariabarbosa.enums.FormaPagamento;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.repository.PagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoService pedidoService;

    public Pagamento registrarPagamento(Long pedidoId, FormaPagamento formaPagamento, BigDecimal valor) {
        if (pagamentoRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalArgumentException("Pedido ja possui pagamento registrado");
        }

        var pedido = pedidoService.buscarPorId(pedidoId);
        BigDecimal valorPagamento = valor != null ? valor : pedido.getTotal();

        Pagamento pagamento = Pagamento.builder()
                .pedido(pedido)
                .formaPagamento(formaPagamento)
                .valor(valorPagamento)
                .pagoEm(LocalDateTime.now())
                .build();

        pedido.setPagamento(pagamento);
        pedido.setStatus(StatusPedido.ENTREGUE);
        pedidoService.salvar(pedido);
        return pagamento;
    }
}
