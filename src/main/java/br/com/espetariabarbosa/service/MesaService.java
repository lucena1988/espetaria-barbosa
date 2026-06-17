package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Mesa;
import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.enums.StatusMesa;
import br.com.espetariabarbosa.enums.StatusPedido;
import br.com.espetariabarbosa.repository.MesaRepository;
import br.com.espetariabarbosa.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;

    private static final List<StatusPedido> STATUS_PEDIDOS_ATIVOS = List.of(
            StatusPedido.RECEBIDO,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO
    );

    public List<Mesa> listarAtivas() {
        return mesaRepository.findByAtivaTrueOrderByNumeroAsc();
    }

    public List<Mesa> listarInativas() {
        return mesaRepository.findByAtivaFalseOrderByNumeroAsc();
    }

    public List<Mesa> listarDisponiveisParaPedido() {
        return mesaRepository.findByStatusInAndAtivaTrueOrderByNumeroAsc(
                List.of(StatusMesa.LIVRE, StatusMesa.OCUPADA)
        );
    }

    public Mesa salvar(Mesa mesa) {
        return mesaRepository.save(mesa);
    }

    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa nao encontrada"));
    }

    public void alterarStatus(Long id, StatusMesa status) {
        Mesa mesa = buscarPorId(id);
        mesa.setStatus(status);
        mesaRepository.save(mesa);
    }

    @Transactional
    public void renomear(Long id, String numero, String descricao) {
        Mesa mesa = buscarPorId(id);
        String numeroAtual = mesa.getNumero();
        String novoNumero = normalizarNumero(numero);

        if (!novoNumero.equals(numeroAtual) && mesaRepository.existsByNumero(novoNumero)) {
            throw new RuntimeException("Ja existe uma mesa cadastrada com esse nome");
        }

        mesa.setNumero(novoNumero);
        mesa.setDescricao(normalizarDescricao(descricao));
        mesaRepository.save(mesa);

        if (!novoNumero.equals(numeroAtual)) {
            List<Pedido> pedidosAtivos = pedidoRepository.findByMesaAndStatusIn(numeroAtual, STATUS_PEDIDOS_ATIVOS);
            pedidosAtivos.forEach(pedido -> pedido.setMesa(novoNumero));
            pedidoRepository.saveAll(pedidosAtivos);
        }
    }

    @Transactional
    public void excluir(Long id) {
        Mesa mesa = buscarPorId(id);

        if (pedidoRepository.existsByMesaAndStatusIn(mesa.getNumero(), STATUS_PEDIDOS_ATIVOS)) {
            throw new RuntimeException("Nao e possivel excluir uma mesa com pedido ativo");
        }

        mesa.setAtiva(false);
        mesa.setStatus(StatusMesa.INATIVA);
        mesaRepository.save(mesa);
    }

    public void reativar(Long id) {
        Mesa mesa = buscarPorId(id);

        if (mesaRepository.findByNumeroAndAtivaTrue(mesa.getNumero()).isPresent()) {
            throw new RuntimeException("Ja existe uma mesa ativa com esse nome");
        }

        mesa.setAtiva(true);
        mesa.setStatus(StatusMesa.LIVRE);
        mesaRepository.save(mesa);
    }

    public void atualizarStatusPorNumeroSeExistir(String numero, StatusMesa status) {
        if (numero == null || numero.isBlank()) {
            return;
        }

        mesaRepository.findByNumeroAndAtivaTrue(numero)
                .ifPresent(mesa -> {
                    mesa.setStatus(status);
                    mesaRepository.save(mesa);
                });
    }

    private String normalizarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new RuntimeException("Informe o nome da mesa");
        }
        return numero.trim();
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }
}
