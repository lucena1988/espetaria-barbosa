package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    boolean existsByPedidoId(Long pedidoId);
}
