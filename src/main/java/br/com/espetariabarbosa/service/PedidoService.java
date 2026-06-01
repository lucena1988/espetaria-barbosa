package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public Pedido buscarPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido nao encontrado"));
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

        List<ItemEstoque> itensEstoque = new ArrayList<>();

        for (int i = 0; i < produtoIds.size(); i++) {
            Long produtoId = produtoIds.get(i);
            Integer quantidade = i < quantidades.size() ? quantidades.get(i) : 0;

            if (produtoId == null || quantidade == null || quantidade <= 0) {
                continue;
            }

            Produto produto = produtoService.buscarPorId(produtoId);
            if (!produto.possuiEstoque(quantidade)) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }
            itensEstoque.add(new ItemEstoque(produto, quantidade));

            ItemPedido item = ItemPedido.builder()
                    .nomeProduto(produto.getNome())
                    .quantidade(quantidade)
                    .precoUnitario(produto.getPreco())
                    .build();

            pedido.adicionarItem(item);
        }

        if (pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um item valido para criar o pedido");
        }

        itensEstoque.forEach(item -> produtoService.baixarEstoque(item.produto(), item.quantidade()));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
        return pedidoSalvo;
    }

    public void alterarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.setStatus(status);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }

    public Pedido salvar(Pedido pedido) {
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
        return pedidoSalvo;
    }

    private record ItemEstoque(Produto produto, Integer quantidade) {
    }
}
