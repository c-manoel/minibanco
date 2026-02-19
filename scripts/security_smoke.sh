#!/usr/bin/env bash
set -u

BASE_URL="${BASE_URL:-http://localhost:8080}"
MB_EMAIL="${MB_EMAIL:-}"
MB_PASSWORD="${MB_PASSWORD:-}"
MB_PIX_DEST_KEY="${MB_PIX_DEST_KEY:-}"

PASS_COUNT=0
FAIL_COUNT=0
SKIP_COUNT=0

log() {
  printf "\n[%s] %s\n" "$1" "$2"
}

pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  log "PASS" "$1"
}

fail() {
  FAIL_COUNT=$((FAIL_COUNT + 1))
  log "FAIL" "$1"
}

skip() {
  SKIP_COUNT=$((SKIP_COUNT + 1))
  log "SKIP" "$1"
}

extract_json_string() {
  local body="$1"
  local key="$2"
  echo "$body" | sed -n "s/.*\"$key\":\"\\([^\"]*\\)\".*/\\1/p"
}

request() {
  local method="$1"
  local url="$2"
  local auth="${3:-}"
  local body="${4:-}"

  local response
  if [ -n "$auth" ] && [ -n "$body" ]; then
    response=$(curl -sS -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url" \
      -H "Authorization: Bearer $auth" \
      -H "Content-Type: application/json" \
      -d "$body")
  elif [ -n "$auth" ]; then
    response=$(curl -sS -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url" \
      -H "Authorization: Bearer $auth")
  elif [ -n "$body" ]; then
    response=$(curl -sS -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -d "$body")
  else
    response=$(curl -sS -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url")
  fi

  HTTP_STATUS=$(echo "$response" | sed -n 's/HTTP_STATUS://p')
  HTTP_BODY=$(echo "$response" | sed '/HTTP_STATUS:/d')
}

main() {
  log "INFO" "Base URL: $BASE_URL"

  if [ -z "$MB_EMAIL" ] || [ -z "$MB_PASSWORD" ]; then
    skip "Defina MB_EMAIL e MB_PASSWORD para rodar testes autenticados."
  fi

  log "TEST" "Rate limit de login (espera pelo menos um 429)"
  local got_429=0
  for i in 1 2 3 4 5 6 7; do
    request "POST" "$BASE_URL/auth/login" "" "{\"email\":\"$MB_EMAIL\",\"senha\":\"senha_errada_$i\"}"
    if [ "$HTTP_STATUS" = "429" ]; then
      got_429=1
      break
    fi
  done
  if [ "$got_429" = "1" ]; then
    pass "Rate limit de login ativo."
  else
    fail "Nao houve 429 no teste de brute force de login."
  fi

  if [ -z "$MB_EMAIL" ] || [ -z "$MB_PASSWORD" ]; then
    log "INFO" "Resumo parcial sem testes autenticados."
    print_summary
    exit 1
  fi

  log "TEST" "Login válido"
  request "POST" "$BASE_URL/auth/login" "" "{\"email\":\"$MB_EMAIL\",\"senha\":\"$MB_PASSWORD\"}"
  if [ "$HTTP_STATUS" != "200" ]; then
    fail "Login valido falhou (status $HTTP_STATUS)."
    print_summary
    exit 1
  fi
  pass "Login válido ok."

  ACCESS_TOKEN="$(extract_json_string "$HTTP_BODY" "accessToken")"
  REFRESH_TOKEN="$(extract_json_string "$HTTP_BODY" "refreshToken")"
  if [ -z "$ACCESS_TOKEN" ] || [ -z "$REFRESH_TOKEN" ]; then
    fail "Tokens não retornados no login."
    print_summary
    exit 1
  fi
  pass "Access/Refresh token recebidos."

  log "TEST" "Endpoint protegido com access token"
  request "GET" "$BASE_URL/contas/saldo" "$ACCESS_TOKEN"
  if [ "$HTTP_STATUS" = "200" ]; then
    pass "Acesso protegido com token válido."
  else
    fail "Falha no acesso protegido (status $HTTP_STATUS)."
  fi

  log "TEST" "Refresh com token inválido"
  request "POST" "$BASE_URL/auth/refresh" "" "{\"refreshToken\":\"invalido\"}"
  if [ "$HTTP_STATUS" = "401" ]; then
    pass "Refresh inválido corretamente bloqueado."
  else
    fail "Refresh inválido deveria retornar 401 (veio $HTTP_STATUS)."
  fi

  log "TEST" "Refresh com token válido"
  request "POST" "$BASE_URL/auth/refresh" "" "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
  if [ "$HTTP_STATUS" = "200" ]; then
    pass "Refresh válido ok."
    ACCESS_TOKEN="$(extract_json_string "$HTTP_BODY" "accessToken")"
  else
    fail "Refresh válido falhou (status $HTTP_STATUS)."
  fi

  log "TEST" "Auditoria de tentativas de login"
  request "GET" "$BASE_URL/auth/login-attempts" "$ACCESS_TOKEN"
  if [ "$HTTP_STATUS" = "200" ]; then
    pass "Auditoria disponível para usuário autenticado."
  else
    fail "Falha em /auth/login-attempts (status $HTTP_STATUS)."
  fi

  if [ -n "$MB_PIX_DEST_KEY" ]; then
    log "TEST" "PIX alto sem confirmação extra"
    request "POST" "$BASE_URL/contas/pix/preview" "$ACCESS_TOKEN" \
      "{\"chaveDestino\":\"$MB_PIX_DEST_KEY\",\"valor\":1500.00,\"confirmacaoExtra\":false}"
    if [ "$HTTP_STATUS" = "400" ]; then
      pass "Regra de confirmação extra para PIX alto ativa."
    else
      fail "PIX alto sem confirmação deveria falhar com 400 (veio $HTTP_STATUS)."
    fi
  else
    skip "MB_PIX_DEST_KEY não definido. Teste de PIX alto foi pulado."
  fi

  print_summary
  if [ "$FAIL_COUNT" -gt 0 ]; then
    exit 1
  fi
}

print_summary() {
  log "SUMMARY" "PASS=$PASS_COUNT FAIL=$FAIL_COUNT SKIP=$SKIP_COUNT"
}

main "$@"
