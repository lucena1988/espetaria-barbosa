package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Pedido;
import br.com.espetariabarbosa.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByStatusInOrderByCriadoEmAsc(List<StatusPedido> status);
    List<Pedido> findAllByOrderByCriadoEmDesc();
}
