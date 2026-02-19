# MiniBanco

Projeto de estudo em **backend e frontend** que simula uma instituição financeira digital, com foco em:

- Arquitetura de **API REST**
- Autenticação com **JWT (access e refresh token)**
- **Segurança aplicada** (rate limiting, auditoria e regras antifraude)
- Fluxo bancário de **PIX com múltiplas etapas de confirmação**

---

## Tecnologias

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT

### Frontend
- HTML, CSS e JavaScript
- Vite

---

## Funcionalidades

- Cadastro de conta com validação de CPF e score
- Autenticação com access token e refresh token
- Controle de sessão com expiração visível no frontend
- Renovação automática de sessão
- Perfil do usuário (nome, e-mail e CPF mascarado)
- Alteração de senha
- Consulta de saldo
- Depósito
- Operações PIX com:
  - limite diário
  - validações antifraude
  - confirmação extra para valores elevados
  - pré-visualização da operação
  - confirmação com senha
- Cadastro e listagem de chaves PIX
- Extrato com filtros, paginação e exportação em CSV
- Sistema de notificações com filtro por tipo e status de leitura
- Auditoria de tentativas de login

---

## Estrutura do Projeto

- `src/main/java/` — backend Spring Boot
- `src/main/resources/` — configurações da aplicação
- `frontend/` — aplicação web
- `API_ENDPOINTS.md` — documentação dos endpoints da API
- `SECURITY_PLAYBOOK.md` — roteiro de testes de segurança
- `scripts/security_smoke.sh` — smoke test de segurança

---

## Como Executar

### 1) Backend

#### Pré-requisitos
- Java 17 ou superior
- MySQL em execução

#### Configuração
Edite o arquivo `src/main/resources/application.properties` e configure:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `jwt.secret`

#### Execução
```bash
./mvnw spring-boot:run
