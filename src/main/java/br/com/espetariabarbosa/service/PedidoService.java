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
    private final PedidoWebSocketService pedidoWebSocketService;
    private final ClienteService clienteService;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc();
    }

    public List<Pedido> listarAtivos() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(
                List.of(StatusPedido.RECEBIDO, StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
        );
    }

    public List<Pedido> listarHistorico() {
        return pedidoRepository.findByStatusInOrderByCriadoEmDesc(
                List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO)
        );
    }

    public List<Pedido> listarPainel() {
        return listarAtivos();
    }

    public List<Pedido> listarPainelTv() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(
                List.of(StatusPedido.EM_PREPARO, StatusPedido.PRONTO)
        );
    }

    public Pedido criarPedido(String nomeCliente, String mesa, TipoAtendimento tipoAtendimento,
                              List<Long> produtoIds, List<Integer> quantidades) {
        return criarPedido(null, nomeCliente, mesa, tipoAtendimento, produtoIds, quantidades);
    }

    public Pedido criarPedido(Long clienteId, String nomeCliente, String mesa, TipoAtendimento tipoAtendimento,
                              List<Long> produtoIds, List<Integer> quantidades) {
        var cliente = clienteId != null ? clienteService.buscarPorId(clienteId) : null;
        String nomeClientePedido = cliente != null ? cliente.getNome() : nomeCliente;

        if (nomeClientePedido == null || nomeClientePedido.isBlank()) {
            throw new IllegalArgumentException("Informe um cliente para criar o pedido");
        }

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .nomeCliente(nomeClientePedido)
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

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
        return pedidoSalvo;
    }

    public void alterarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setStatus(status);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }
}
