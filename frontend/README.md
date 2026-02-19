# MiniBanco Frontend

Frontend separado (Vite) para consumir a API do MiniBanco.

## Requisitos
- Node.js 18+
- Backend rodando em `http://localhost:8080`

## Rodar localmente
```bash
npm install
npm run dev
```

Abra `http://localhost:5173`.

## Criar repositório separado
No diretório `frontend`:
```bash
git init
git add .
git commit -m "feat(front): fluxo minimo de login, saldo, deposito, pix e extrato"
git branch -M main
git remote add origin <URL_DO_REPO_FRONT>
git push -u origin main
```
