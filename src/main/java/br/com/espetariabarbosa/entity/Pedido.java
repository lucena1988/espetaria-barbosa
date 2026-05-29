package br.com.espetariabarbosa.entity;

import br.com.espetariabarbosa.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCliente;

    private String mesa;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private LocalDateTime criadoEm;

    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        criadoEm = LocalDateTime.now();
        if (status == null) {
            status = StatusPedido.RECEBIDO;
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }
}
