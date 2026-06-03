package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.ItemPedido;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.entity.Produto;
import br.com.espetariabarbosa.enums.StatusMesa;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.enums.TipoAtendimento;
import br.com.espetariabarbosa.repository.ItemPedidoRepository;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final List<StatusPedido> STATUS_ATIVOS = List.of(
            StatusPedido.RECEBIDO,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO
    );

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;
    private final PedidoWebSocketService pedidoWebSocketService;
    private final ClienteService clienteService;
    private final MesaService mesaService;
    private final ItemPedidoRepository itemPedidoRepository;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByCriadoEmDesc();
    }

    public List<Pedido> listarAtivos() {
        return pedidoRepository.findByStatusInOrderByCriadoEmAsc(STATUS_ATIVOS);
    }

    public List<Pedido> listarHistorico() {
        return pedidoRepository.findByStatusInOrderByCriadoEmDesc(
                List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO)
        ).stream()
                .filter(pedido -> !Boolean.TRUE.equals(pedido.getOcultoHistorico()))
                .toList();
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

    @Transactional
    public void ocultarDoHistorico(Long pedidoId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (!List.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO).contains(pedido.getStatus())) {
            throw new IllegalArgumentException("So e possivel ocultar pedidos finalizados do historico");
        }

        pedido.setOcultoHistorico(true);
        pedidoRepository.save(pedido);
    }

    public Pedido criarPedido(String nomeCliente, String mesa, TipoAtendimento tipoAtendimento,
                              List<Long> produtoIds, List<Integer> quantidades) {
        return criarPedido(null, nomeCliente, mesa, tipoAtendimento, produtoIds, quantidades);
    }

    @Transactional
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
                    .produtoId(produto.getId())
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
        ocuparMesaSePedidoAtivo(pedidoSalvo);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
        return pedidoSalvo;
    }

    @Transactional
    public void alterarStatus(Long pedidoId, StatusPedido status) {
        Pedido pedido = buscarPorId(pedidoId);
        pedido.setStatus(status);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        atualizarStatusMesaDepoisDoStatus(pedidoSalvo);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }

    @Transactional
    public void transferirMesa(Long pedidoId, String novaMesa) {
        if (novaMesa == null || novaMesa.isBlank()) {
            throw new IllegalArgumentException("Informe a nova mesa para transferir o pedido");
        }

        Pedido pedido = buscarPorId(pedidoId);
        String mesaAnterior = pedido.getMesa();

        if (novaMesa.equals(mesaAnterior)) {
            return;
        }

        pedido.setMesa(novaMesa);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        liberarMesaSeNaoTemPedidoAtivo(mesaAnterior);
        ocuparMesaSePedidoAtivo(pedidoSalvo);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }

    @Transactional
    public void adicionarItem(Long pedidoId, Long produtoId, Integer quantidade) {
        if (produtoId == null) {
            throw new IllegalArgumentException("Selecione um produto para adicionar ao pedido");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade valida");
        }

        Pedido pedido = buscarPorId(pedidoId);
        if (!STATUS_ATIVOS.contains(pedido.getStatus())) {
            throw new IllegalArgumentException("So e possivel adicionar itens em pedidos abertos");
        }

        Produto produto = produtoService.buscarPorId(produtoId);
        if (!produto.possuiEstoque(quantidade)) {
            throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
        }

        ItemPedido item = ItemPedido.builder()
                .nomeProduto(produto.getNome())
                .produtoId(produto.getId())
                .quantidade(quantidade)
                .precoUnitario(produto.getPreco())
                .build();

        item.setPedido(pedido);
        item.calcularSubtotal();
        ItemPedido itemSalvo = itemPedidoRepository.save(item);
        pedido.getItens().add(itemSalvo);
        pedido.recalcularTotal();

        produtoService.baixarEstoque(produto, quantidade);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }

    @Transactional
    public void removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = buscarPorId(pedidoId);
        if (!STATUS_ATIVOS.contains(pedido.getStatus())) {
            throw new IllegalArgumentException("So e possivel remover itens de pedidos abertos");
        }

        ItemPedido item = pedido.getItens().stream()
                .filter(itemPedido -> itemPedido.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item nao encontrado no pedido"));

        produtoService.reporEstoque(item.getProdutoId(), item.getNomeProduto(), item.getQuantidade());
        pedido.getItens().remove(item);
        pedido.recalcularTotal();

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
    }

    @Transactional
    public void juntarPedidos(Long pedidoOrigemId, Long pedidoDestinoId) {
        if (pedidoDestinoId == null) {
            throw new IllegalArgumentException("Selecione a comanda de destino");
        }
        if (pedidoOrigemId.equals(pedidoDestinoId)) {
            throw new IllegalArgumentException("Selecione uma comanda diferente para juntar");
        }

        Pedido origem = buscarPorId(pedidoOrigemId);
        Pedido destino = buscarPorId(pedidoDestinoId);

        if (!STATUS_ATIVOS.contains(origem.getStatus()) || !STATUS_ATIVOS.contains(destino.getStatus())) {
            throw new IllegalArgumentException("So e possivel juntar pedidos abertos");
        }

        origem.getItens().forEach(item -> {
            ItemPedido itemDestino = ItemPedido.builder()
                    .nomeProduto(item.getNomeProduto())
                    .produtoId(item.getProdutoId())
                    .quantidade(item.getQuantidade())
                    .precoUnitario(item.getPrecoUnitario())
                    .build();
            itemDestino.setPedido(destino);
            itemDestino.calcularSubtotal();
            ItemPedido itemSalvo = itemPedidoRepository.save(itemDestino);
            destino.getItens().add(itemSalvo);
            destino.recalcularTotal();
        });

        String mesaOrigem = origem.getMesa();
        origem.getItens().clear();
        origem.setTotal(BigDecimal.ZERO);
        origem.setStatus(StatusPedido.CANCELADO);

        Pedido destinoSalvo = pedidoRepository.save(destino);
        Pedido origemSalva = pedidoRepository.save(origem);
        liberarMesaSeNaoTemPedidoAtivo(mesaOrigem);
        ocuparMesaSePedidoAtivo(destinoSalvo);
        pedidoWebSocketService.notificarAtualizacao(destinoSalvo);
        pedidoWebSocketService.notificarAtualizacao(origemSalva);
    }

    @Transactional
    public Pedido salvar(Pedido pedido) {
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        atualizarStatusMesaDepoisDoStatus(pedidoSalvo);
        pedidoWebSocketService.notificarAtualizacao(pedidoSalvo);
        return pedidoSalvo;
    }

    private void atualizarStatusMesaDepoisDoStatus(Pedido pedido) {
        if (STATUS_ATIVOS.contains(pedido.getStatus())) {
            ocuparMesaSePedidoAtivo(pedido);
            return;
        }

        liberarMesaSeNaoTemPedidoAtivo(pedido.getMesa());
    }

    private void ocuparMesaSePedidoAtivo(Pedido pedido) {
        if (pedido.getTipoAtendimento() == TipoAtendimento.MESA && STATUS_ATIVOS.contains(pedido.getStatus())) {
            mesaService.atualizarStatusPorNumeroSeExistir(pedido.getMesa(), StatusMesa.OCUPADA);
        }
    }

    private void liberarMesaSeNaoTemPedidoAtivo(String mesa) {
        if (mesa == null || mesa.isBlank()) {
            return;
        }

        if (!pedidoRepository.existsByMesaAndStatusIn(mesa, STATUS_ATIVOS)) {
            mesaService.atualizarStatusPorNumeroSeExistir(mesa, StatusMesa.LIVRE);
        }
    }

    private record ItemEstoque(Produto produto, Integer quantidade) {
    }
}
