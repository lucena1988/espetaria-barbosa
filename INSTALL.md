# Instalacao offline - Espetaria Barbosa

Este guia descreve como instalar e operar o sistema em um cliente sem depender de internet no dia a dia.

## Como o sistema roda

O sistema roda em um computador principal da loja:

```text
Computador principal
- Docker Desktop
- Sistema Espetaria Barbosa
- PostgreSQL local
- Backups locais
```

Os outros dispositivos acessam pela rede interna:

```text
Caixa/cozinha/TV/celular -> Wi-Fi local -> computador principal
```

A internet nao e necessaria para operar pedidos, cozinha, painel TV, pagamentos, relatorios e recibos.

## O que precisa estar instalado uma vez

Antes de usar sem internet, o computador principal precisa ter:

- Windows 10 ou superior.
- Docker Desktop instalado e configurado.
- Imagens Docker do sistema e do PostgreSQL ja baixadas.
- Pasta do sistema copiada para o computador.

Depois disso, o sistema pode funcionar apenas com a rede local da loja.

## Primeiro inicio no cliente

1. Abra o Docker Desktop.
2. Abra a pasta do sistema.
3. Execute:

```text
scripts\start.bat
```

4. Acesse no computador principal:

```text
http://localhost:8080
```

5. Login inicial:

```text
Usuario: admin
Senha: admin123
```

6. Cadastre funcionarios, categorias, produtos, estoque e mesas.

## Acesso pela rede interna

No computador principal, descubra o IP local:

```bat
ipconfig
```

Procure o endereco IPv4 da rede Wi-Fi ou cabo. Exemplo:

```text
192.168.0.10
```

Nos outros dispositivos da loja, acesse:

```text
http://192.168.0.10:8080
```

Exemplos:

- Cozinha: `http://192.168.0.10:8080/cozinha`
- Painel TV: `http://192.168.0.10:8080/painel`
- Dashboard: `http://192.168.0.10:8080/dashboard`

## Cardapio por QR Code sem internet

O QR Code da mesa precisa apontar para o endereco local do computador principal.

Exemplo:

```text
http://192.168.0.10:8080/cardapio/mesa/1
```

Importante: se o IP do computador mudar, os QR Codes podem precisar ser gerados novamente.

Para evitar isso, configure IP fixo no computador principal ou reserve o IP no roteador.

## Parar o sistema

Execute:

```text
scripts\stop.bat
```

## Backup

Para gerar backup local do banco:

```text
scripts\backup.bat
```

Os arquivos sao salvos em:

```text
backups\
```

Formato do arquivo:

```text
espetaria-barbosa-AAAAmmdd-HHmmss.dump
```

Recomendacao:

- Fazer backup diariamente.
- Copiar periodicamente os backups para pendrive ou HD externo.
- Nunca enviar backup de cliente para repositorio Git.

## Restaurar backup

Execute:

```text
scripts\restore.bat
```

O script lista os arquivos da pasta `backups\` e pede o nome do backup.

Atencao: restaurar backup substitui os dados atuais do banco.

## Atualizacao do sistema

Para atualizar um cliente offline:

1. Gere uma nova versao do projeto em uma maquina com internet.
2. Leve o pacote atualizado para o cliente.
3. Faca backup antes da troca.
4. Suba a nova versao com:

```text
scripts\start.bat
```

Como o banco usa `ddl-auto=update`, as tabelas sao ajustadas automaticamente em desenvolvimento. Para producao comercial, o ideal e evoluir para Flyway.

## Cuidados importantes

- Troque a senha do usuario `admin` no primeiro acesso.
- Deixe o computador principal ligado durante a operacao.
- Configure o Windows para nao suspender automaticamente.
- Configure o Docker Desktop para iniciar com o Windows.
- Mantenha backup local em outro dispositivo.
- Use PostgreSQL para cliente real. H2 deve ficar apenas para desenvolvimento.

## Solucao de problemas

### Nao abre `http://localhost:8080`

Verifique:

- Docker Desktop esta aberto?
- `scripts\start.bat` foi executado?
- A porta `8080` esta livre?

### Outros dispositivos nao acessam

Verifique:

- Estao na mesma rede Wi-Fi?
- O IP usado esta correto?
- O firewall do Windows permite acesso na porta `8080`?

### Backup falhou

Verifique:

- O container `espetaria-barbosa-db` esta rodando?
- Existe espaco livre no disco?
- Docker Desktop esta aberto?
