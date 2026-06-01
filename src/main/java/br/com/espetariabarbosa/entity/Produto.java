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
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String categoria;

    private BigDecimal preco;

    @Builder.Default
    private Integer quantidadeEstoque = 0;

    @Builder.Default
    private Integer estoqueMinimo = 5;

    @Builder.Default
    private Boolean ativo = true;

    public boolean possuiEstoque(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            return true;
        }

        return quantidadeEstoque != null && quantidadeEstoque >= quantidade;
    }

    public boolean estoqueBaixo() {
        int estoqueAtual = quantidadeEstoque == null ? 0 : quantidadeEstoque;
        int minimo = estoqueMinimo == null ? 0 : estoqueMinimo;
        return estoqueAtual > 0 && estoqueAtual <= minimo;
    }

    public boolean esgotado() {
        return quantidadeEstoque == null || quantidadeEstoque <= 0;
    }
}
