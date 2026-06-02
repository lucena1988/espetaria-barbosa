# Espetaria Barbosa - Gestao de Pedidos

Sistema web para operacao de pedidos, comandas, cozinha, mesas, estoque, clientes, pagamentos e relatorios da Espetaria Barbosa.

## Tecnologias

- Java 17
- Spring Boot 3.3
- Spring Security
- Spring Data JPA
- Thymeleaf
- WebSocket com SockJS/STOMP
- PostgreSQL no Docker
- H2 para desenvolvimento local
- PDFBox para extratos e recibos
- ZXing para QR Code das mesas

## Funcionalidades

- Login com usuario administrador inicial.
- Cadastro de funcionarios com perfil `ADMIN` ou `FUNCIONARIO`.
- Cadastro de clientes.
- Cadastro de mesas com status e QR Code para cardapio digital.
- Cadastro de categorias de produtos.
- Cadastro de produtos com controle de estoque e estoque minimo.
- Criacao de pedidos por mesa, balcao, delivery ou retirada.
- Pedido com multiplos itens.
- Adicao de item em pedido aberto.
- Remocao de item de pedido aberto com reposicao de estoque.
- Transferencia de pedido entre mesas.
- Juncao de comandas/pedidos abertos.
- Painel da cozinha com status `RECEBIDO`, `EM_PREPARO` e `PRONTO`.
- Painel TV com atualizacao em tempo real e alerta sonoro para pedidos prontos.
- Historico de pedidos entregues e cancelados.
- Fechamento de conta com desconto, taxa de servico e pagamento dividido.
- Dashboard financeiro com filtro por dia, mes ou intervalo de datas.
- Ocultar/exibir valores financeiros na tela do dashboard.
- Extrato financeiro em PDF com marca d'agua da logo.
- Recibo PDF por pedido finalizado.

## Acesso inicial

Ao iniciar o sistema pela primeira vez, um administrador padrao e criado automaticamente:

```text
Usuario: admin
Senha: admin123
```

Depois do primeiro acesso, cadastre os funcionarios reais em:

```text
http://localhost:8080/funcionarios
```

## Como executar com Docker

Suba a aplicacao e o PostgreSQL:

```bash
docker compose up --build
```

Acesse:

```text
http://localhost:8080
```

Para rodar em segundo plano:

```bash
docker compose up --build -d
```

Para parar:

```bash
docker compose down
```

Para apagar tambem os dados locais do PostgreSQL:

```bash
docker compose down -v
```

## Banco PostgreSQL no Docker

```text
Host: localhost
Porta: 5432
Database: espetaria_barbosa
User: espetaria
Password: espetaria
```

## Como executar localmente pelo IntelliJ

1. Abra o projeto no IntelliJ.
2. Aguarde o Maven baixar as dependencias.
3. Execute a classe:

```text
EspetariaBarbosaApplication.java
```

4. Acesse:

```text
http://localhost:8080
```

Nesse modo, o sistema usa H2 em memoria.

## Banco H2 local

```text
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:espetaria
User: sa
Password: deixe vazio
```

## Principais telas

- Login: `http://localhost:8080/login`
- Dashboard: `http://localhost:8080/dashboard`
- Extrato PDF: `http://localhost:8080/dashboard/extrato.pdf`
- Pedidos em aberto: `http://localhost:8080/pedidos`
- Novo pedido: `http://localhost:8080/pedidos/novo`
- Historico: `http://localhost:8080/pedidos/historico`
- Recibo PDF: `http://localhost:8080/pedidos/{id}/recibo.pdf`
- Clientes: `http://localhost:8080/clientes`
- Mesas: `http://localhost:8080/mesas`
- QR Code da mesa: `http://localhost:8080/mesas/{id}/qrcode`
- Cardapio digital da mesa: `http://localhost:8080/cardapio/mesa/{id}`
- Categorias: `http://localhost:8080/categorias`
- Produtos: `http://localhost:8080/produtos`
- Funcionarios: `http://localhost:8080/funcionarios`
- Cozinha: `http://localhost:8080/cozinha`
- Painel TV: `http://localhost:8080/painel`

## Fluxo sugerido de uso

1. Acesse com `admin` / `admin123`.
2. Cadastre funcionarios.
3. Cadastre categorias.
4. Cadastre produtos e estoque.
5. Cadastre mesas.
6. Use o QR Code da mesa para abrir o cardapio digital.
7. Crie pedidos pela tela interna ou pelo cardapio.
8. Acompanhe preparo na cozinha.
9. Use o painel TV para pedidos em preparo e prontos.
10. Feche a conta com desconto, taxa e formas de pagamento.
11. Consulte historico, recibos e extratos no dashboard.

## Validacao

Para compilar e rodar os testes:

```bash
mvn test
```

Para validar a aplicacao completa com PostgreSQL:

```bash
docker compose up --build -d
```

## Observacoes

- O banco usa `spring.jpa.hibernate.ddl-auto=update`, entao as tabelas sao ajustadas automaticamente em desenvolvimento.
- O painel da cozinha e o painel TV recebem atualizacoes via WebSocket.
- O cardapio digital em `/cardapio/**` e publico para permitir uso por QR Code.
- A tela de funcionarios e restrita ao perfil `ADMIN`.
