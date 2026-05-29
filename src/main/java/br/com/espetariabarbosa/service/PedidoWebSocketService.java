package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notificarAtualizacao(Pedido pedido) {
        messagingTemplate.convertAndSend("/topic/pedidos", new PedidoEvento(
                pedido.getId(),
                pedido.getStatus().name()
        ));
    }

    public record PedidoEvento(Long pedidoId, String status) {
    }
}
