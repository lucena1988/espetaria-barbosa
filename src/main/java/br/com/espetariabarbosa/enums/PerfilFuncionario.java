package br.com.espetariabarbosa.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PerfilFuncionario {
    ADMIN("Administrador"),
    CAIXA("Caixa"),
    COZINHA("Cozinha"),
    ATENDENTE("Atendente"),
    FUNCIONARIO("Funcionario");

    private final String descricao;
}
