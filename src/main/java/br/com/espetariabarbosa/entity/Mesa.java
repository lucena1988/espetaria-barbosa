package br.com.espetariabarbosa.entity;

import br.com.espetariabarbosa.enums.StatusMesa;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    private String descricao;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private StatusMesa status = StatusMesa.LIVRE;

    @Builder.Default
    private Boolean ativa = true;

    @PrePersist
    @PreUpdate
    public void antesDeSalvar() {
        if (status == null) {
            status = StatusMesa.LIVRE;
        }
        if (ativa == null) {
            ativa = true;
        }
    }
}
