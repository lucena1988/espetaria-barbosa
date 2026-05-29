# Espetaria Barbosa - Sistema de Pedidos

Sistema base em Java 17 + Spring Boot para gestão de pedidos, comandas simples, painel da cozinha e painel estilo McDonald's.

## Funcionalidades

- Cadastro de produtos
- Criação de pedidos
- Listagem de pedidos
- Alteração de status
- Painel da cozinha
- Painel público para TV

## Como executar

1. Abra o projeto no IntelliJ
2. Aguarde o Maven baixar as dependências
3. Execute a classe:

```text
EspetariaBarbosaApplication.java
```

4. Acesse:

```text
http://localhost:8080
```

## Telas

- Pedidos: `http://localhost:8080/pedidos`
- Novo pedido: `http://localhost:8080/pedidos/novo`
- Produtos: `http://localhost:8080/produtos`
- Cozinha: `http://localhost:8080/cozinha`
- Painel TV: `http://localhost:8080/painel`
- H2 Console: `http://localhost:8080/h2-console`

## Banco H2

Use:

```text
JDBC URL: jdbc:h2:mem:espetaria
User: sa
Password: deixe vazio
```

## Próximas melhorias

- Login com Spring Security
- Comanda com vários itens no mesmo pedido
- Controle de mesas
- Pagamento
- Relatórios
- WebSocket para atualização em tempo real
- PostgreSQL em produção
