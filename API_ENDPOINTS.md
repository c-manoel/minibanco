# API Minibanco - Contrato de Resposta

Base URL: `http://localhost:8080`

## Erro (padrão)
Todas as falhas retornam este formato:

```json
{
  "timestamp": "2026-02-18T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descrição do erro",
  "path": "/contas/pix"
}
```

## Endpoints

### `POST /auth/login`
- Auth: não
- Request:
```json
{
  "email": "carlos3@teste.com",
  "senha": "123456"
}
```
- Response `200`:
```json
{
  "token": "jwt..."
}
```
- Erros comuns: `401` (credenciais inválidas)

### `POST /contas`
- Auth: não
- Request:
```json
{
  "nome": "Carlos",
  "email": "carlos3@teste.com",
  "senha": "123456"
}
```
- Response `201`:
```json
{
  "id": 1,
  "nome": "Carlos",
  "email": "carlos3@teste.com",
  "saldo": 0
}
```

### `GET /contas/saldo`
- Auth: Bearer token
- Response `200`:
```json
{
  "saldo": 90.00
}
```

### `POST /contas/deposito`
- Auth: Bearer token
- Request:
```json
{
  "valor": 100.00
}
```
- Response `200`:
```json
{
  "mensagem": "Depósito realizado com sucesso"
}
```
- Erros comuns: `400` (valor inválido)

### `POST /contas/pix`
- Auth: Bearer token
- Request:
```json
{
  "emailDestino": "destino3@teste.com",
  "valor": 10.00
}
```
- Response `200`:
```json
{
  "mensagem": "PIX realizado com sucesso"
}
```
- Erros comuns:
  - `400` (saldo insuficiente, valor inválido)
  - `404` (conta origem ou destino não encontrada)

### `GET /ping`
- Auth: não
- Response `200`:
```text
API do MiniBanco está funcionando
```
