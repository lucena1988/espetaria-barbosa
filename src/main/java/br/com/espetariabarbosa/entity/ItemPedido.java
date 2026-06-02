package br.com.espetariabarbosa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeProduto;

    private Long produtoId;

    private Integer quantidade;

    private BigDecimal precoUnitario;

    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    public void calcularSubtotal() {
        if (precoUnitario == null || quantidade == null) {
            subtotal = BigDecimal.ZERO;
            return;
        }

        subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
