package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Pedido criarPedido(String nomeCliente, String mesa, TipoAtendimento tipoAtendimento,
                              List<Long> produtoIds, List<Integer> quantidades) {
        Pedido pedido = Pedido.builder()
                .nomeCliente(nomeCliente)
                .mesa(mesa)
                .tipoAtendimento(tipoAtendimento)
                .status(StatusPedido.RECEBIDO)
                .build();

        if (produtoIds == null || quantidades == null || produtoIds.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um produto para criar o pedido");
        }

        for (int i = 0; i < produtoIds.size(); i++) {
            Long produtoId = produtoIds.get(i);
            Integer quantidade = i < quantidades.size() ? quantidades.get(i) : 0;

            if (produtoId == null || quantidade == null || quantidade <= 0) {
                continue;
            }

            Produto produto = produtoService.buscarPorId(produtoId);

            ItemPedido item = ItemPedido.builder()
                    .nomeProduto(produto.getNome())
                    .quantidade(quantidade)
                    .precoUnitario(produto.getPreco())
                    .build();

            pedido.adicionarItem(item);
        }

        if (pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um item válido para criar o pedido");
        }

        return pedidoRepository.save(pedido);
    }

    public void alterarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setStatus(status);
        pedidoRepository.save(pedido);
    }
}
