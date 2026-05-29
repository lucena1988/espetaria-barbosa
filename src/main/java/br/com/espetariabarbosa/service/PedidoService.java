package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc();
    }

    public List<Pedido> listarPainel() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(
                List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
        );
    }

    public Pedido criarPedido(String nomeCliente, String mesa, Long produtoId, Integer quantidade) {
        Produto produto = produtoService.buscarPorId(produtoId);

        BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));

        Pedido pedido = Pedido.builder()
                .nomeCliente(nomeCliente)
                .mesa(mesa)
                .status(StatusPedido.RECEBIDO)
                .total(subtotal)
                .build();

        ItemPedido item = ItemPedido.builder()
                .nomeProduto(produto.getNome())
                .quantidade(quantidade)
                .precoUnitario(produto.getPreco())
                .subtotal(subtotal)
                .pedido(pedido)
                .build();

        pedido.getItens().add(item);

        return pedidoRepository.save(pedido);
    }

    public void alterarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setStatus(status);
        pedidoRepository.save(pedido);
    }
}
