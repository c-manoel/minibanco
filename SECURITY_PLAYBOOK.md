# MiniBanco Security Playbook (Lab Local)

Uso permitido: somente no seu ambiente local/autorizado.

## 1. Preparar o lab
1. Suba backend e banco localmente.
2. Crie 2 contas de teste.
3. Gere token para as 2 contas.
4. Ative logs de segurança no backend.

## 2. Checklist de testes (OWASP API foco prático)
1. Brute force de login.
2. Reuso de refresh token.
3. Token expirado e token inválido.
4. Acesso a recursos com token de outro usuário.
5. Validação de payload malformado.
6. Bypass em regras de PIX (limite diário, frequência, valor alto).
7. Tentativa de confirmar PIX com senha errada.
8. Tentativa de confirmar PIX com operação expirada.
9. Exportação de extrato com filtros extremos.
10. Conferir logs/auditoria para cada tentativa.

## 3. Casos de teste com comandos
1. Login inválido em loop (esperado: `429` após limite):
```bash
for i in {1..8}; do
  curl -s -o /dev/null -w "try $i => %{http_code}\n" \
    -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"carlos3@teste.com","senha":"errada"}'
done
```

2. Refresh com token inválido (esperado: `401`):
```bash
curl -i -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"invalido"}'
```

3. PIX alto sem confirmação extra (esperado: `400`):
```bash
curl -i -X POST http://localhost:8080/contas/pix/preview \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"chaveDestino":"destino3@teste.com","valor":1500.00,"confirmacaoExtra":false}'
```

4. Confirmar PIX com senha errada (esperado: `401`):
```bash
curl -i -X POST http://localhost:8080/contas/pix/confirmar \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"numeroOperacao":"PIX-XXXX","senha":"errada"}'
```

5. Token adulterado (esperado: `401/403`):
```bash
curl -i http://localhost:8080/contas/saldo \
  -H "Authorization: Bearer token.adulterado.aqui"
```

## 4. O que registrar em cada teste
1. Endpoint.
2. Payload.
3. Resultado esperado.
4. Resultado real.
5. Evidência (status + resposta).
6. Risco (baixo/médio/alto).
7. Correção proposta.

## 5. Fluxo de correção (sempre)
1. Reproduzir.
2. Corrigir no backend/frontend.
3. Criar teste automatizado.
4. Rodar regressão.
5. Repetir o ataque e confirmar bloqueio.

## 6. Prioridade de hardening (próximas melhorias)
1. Persistir refresh token no banco com revogação.
2. Persistir auditoria de login no banco.
3. Alertas de fraude com score de risco por evento.
4. Bloqueio temporário de PIX por comportamento suspeito.
5. MFA para confirmação de PIX alto.

## 7. Script automatizado (smoke test)
Arquivo: `scripts/security_smoke.sh`

Exemplo de uso:
```bash
MB_EMAIL="carlos3@teste.com" \
MB_PASSWORD="123456" \
MB_PIX_DEST_KEY="destino3@teste.com" \
./scripts/security_smoke.sh
```

Variáveis suportadas:
- `BASE_URL` (default: `http://localhost:8080`)
- `MB_EMAIL`
- `MB_PASSWORD`
- `MB_PIX_DEST_KEY` (opcional para testar PIX alto sem confirmação extra)
