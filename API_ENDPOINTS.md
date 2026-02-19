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
  "token": "jwt_access...",
  "accessToken": "jwt_access...",
  "refreshToken": "jwt_refresh...",
  "accessTokenExpiresAt": 1771452552000,
  "refreshTokenExpiresAt": 1772662152000,
  "tokenType": "Bearer"
}
```
- Erros comuns:
  - `401` (credenciais inválidas)
  - `429` (muitas tentativas de login no intervalo)

### `POST /auth/refresh`
- Auth: não
- Request:
```json
{
  "refreshToken": "jwt_refresh..."
}
```
- Response `200`: mesmo formato do login (novos tokens)
- Erros comuns: `401` (refresh inválido/expirado)

### `GET /auth/login-attempts`
- Auth: Bearer token
- Response `200`:
```json
[
  {
    "timestamp": "2026-02-18T15:00:00-03:00",
    "email": "carlos3@teste.com",
    "ip": "127.0.0.1",
    "status": "SUCCESS",
    "reason": "Login realizado"
  }
]
```

### `POST /contas`
- Auth: não
- Request:
```json
{
  "nome": "Carlos",
  "cpf": "12345678909",
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
- Erros comuns:
  - `400` (nome/email/senha/cpf inválidos, score insuficiente)
  - `409` (email já cadastrado)

### `GET /contas/perfil`
- Auth: Bearer token
- Response `200`:
```json
{
  "nome": "Carlos",
  "email": "carlos3@teste.com",
  "cpfMascarado": "***.456.***-**"
}
```

### `POST /contas/perfil/senha`
- Auth: Bearer token
- Request:
```json
{
  "senhaAtual": "123456",
  "novaSenha": "654321"
}
```
- Response `200`:
```json
{
  "mensagem": "Senha alterada com sucesso"
}
```
- Erros comuns:
  - `401` (senha atual inválida)
  - `400` (nova senha inválida ou igual à atual)

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
  "valor": 10.00,
  "confirmacaoExtra": false
}
```
- Response `200`:
```json
{
  "mensagem": "PIX realizado com sucesso"
}
```
- Erros comuns:
  - `400` (saldo insuficiente, valor inválido, limite diário, antifraude/frequência)
  - `404` (conta origem ou destino não encontrada)

Regras adicionais de PIX:
- limite diário por conta (`pix.rules.daily-limit`)
- PIX de valor alto exige `confirmacaoExtra=true` (`pix.rules.high-value-threshold`)
- bloqueio por alta frequência de envios (`pix.rules.max-transfers-per-minute`)

### `POST /contas/pix/chaves`
- Auth: Bearer token
- Request:
```json
{
  "tipo": "EMAIL",
  "chave": "destino3@teste.com"
}
```
- Response `201`:
```json
{
  "id": 1,
  "tipo": "EMAIL",
  "chaveMascarada": "***.com"
}
```

### `GET /contas/pix/chaves`
- Auth: Bearer token
- Response `200`:
```json
[
  {
    "id": 1,
    "tipo": "EMAIL",
    "chaveMascarada": "***.com"
  }
]
```

### `POST /contas/pix/preview`
- Auth: Bearer token
- Request:
```json
{
  "chaveDestino": "destino3@teste.com",
  "valor": 10.00,
  "confirmacaoExtra": false
}
```
- Response `200`:
```json
{
  "numeroOperacao": "PIX-ABC12345",
  "valor": 10.00,
  "destinoNome": "Destino",
  "destinoCpfMascarado": "***.123.***-**",
  "destinoChaveMascarada": "***.com",
  "expiraEm": "2026-02-18T16:45:00"
}
```

### `POST /contas/pix/confirmar`
- Auth: Bearer token
- Request:
```json
{
  "numeroOperacao": "PIX-ABC12345",
  "senha": "123456"
}
```
- Response `200`:
```json
{
  "mensagem": "PIX confirmado e realizado com sucesso"
}
```

### `GET /contas/extrato`
- Auth: Bearer token
- Response `200`:
```json
[
  {
    "tipo": "PIX_ENVIADO",
    "valor": 10.00,
    "dataHora": "2026-02-18T14:30:00",
    "emailOrigem": "carlos3@teste.com",
    "emailDestino": "destino3@teste.com"
  }
]
```

### `GET /ping`
- Auth: não
- Response `200`:
```text
API do MiniBanco está funcionando
```
