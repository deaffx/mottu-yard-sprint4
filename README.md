# Mottu Yard Web Application

Sistema de **gerenciamento inteligente do pátio de motos da Mottu** desenvolvido como projeto acadêmico para o Challenge da disciplina **Java Advanced - Sprint 4** da FIAP.

## 👥 Equipe
- **Thiago Moreno Matheus** (RM554507)
- **Celso Canaveze Teixeira Pinto** (RM556118)

## 📋 Sobre o Projeto

Esta é uma aplicação web Spring Boot completa com **Thymeleaf** que permite gerenciar motos, pátios e manutenções. O sistema utiliza **PostgreSQL via Docker** e implementa todas as funcionalidades requeridas para o Sprint 4.

### 🎯 Funcionalidades Principais

- **Autenticação GitHub OAuth2**: Login usando contas autorizadas da Mottu
- **Dashboard**: Métricas em tempo real
- **Gerenciamento de Motos**: CRUD completo com validações
- **Gestão de Pátios**: Controle de capacidade e ocupação
- **Gestão de Motos**: Ocupação de motos separadas por setores
- **Manutenções**: Agendamento e controle de manutenções

### 🛠️ Tecnologias Utilizadas

- **Backend**: Spring Boot 3.5.4, Java 17
- **Frontend**: Thymeleaf
- **Autenticação**: Spring Security + OAuth2 Client (GitHub)
- **Banco de Dados**: PostgreSQL 15+ via Docker
- **Migrations**: Flyway
- **Build**: Gradle
- **Containerização**: Docker Desktop

## 🚀 Instruções de Execução

### Pré-requisitos

1. **Java 17+** instalado
2. **Docker Desktop** instalado e rodando

### Setup Rápido

1. **Clone o repositório** (se necessário)
   ```bash
   git clone <repo-url>
   cd mottu-yard
   ```

2. **Inicie o PostgreSQL via Docker**
   ```bash
   docker-compose up -d
   ```

3. **Configure as credenciais do GitHub OAuth2**
    - Acesse [GitHub Developer Settings → OAuth Apps](https://github.com/settings/developers)
    - Crie um novo **OAuth App** com os valores:
       - *Homepage URL*: `http://localhost:8080`
       - *Authorization callback URL*: `http://localhost:8080/login/oauth2/code/github`
    - Copie o **Client ID** e gere um **Client Secret**.
    - Duplique o arquivo `.env.example` para `.env` e preencha com os valores obtidos:
      ```bash
      cp .env.example .env
      # edite .env e informe GITHUB_CLIENT_ID e GITHUB_CLIENT_SECRET
      ```
    - Alternativamente, defina as variáveis direto no terminal antes de iniciar a aplicação:
      ```powershell
      $env:GITHUB_CLIENT_ID="seu-client-id"
      $env:GITHUB_CLIENT_SECRET="seu-client-secret"
      ```

4. **Verifique se o banco está rodando**
   ```bash
   docker ps
   # Deve mostrar o container em execução
   ```

5. **Execute a aplicação**
   - Abra o projeto
   - Pressione `Ctrl+F5` ou clique em "Run Java" na classe `MottuYardWebApplication`
   - Ou pelo terminal: `./gradlew bootRun`

6. **Acesse a aplicação**
   - URL: http://localhost:8080
   - Faça login com sua conta GitHub autorizada

### 🎮 Dados de Teste

O sistema já vem com dados iniciais populados através das migrations. A autenticação é realizada exclusivamente via GitHub OAuth2, portanto não há mais credenciais fixas de demonstração.

#### Motos de Exemplo
- Honda CG 160 Start (MOT0001)
- Honda PCX 150 (MOT0002) 
- Yamaha YBR 125 Factor (YBR1234)
- KTM Duke 200 (KTM2001)
- E outras...

#### Pátios Configurados
- Pátio Central São Paulo (100 vagas)
- Pátio Zona Sul (75 vagas)
- Pátio ABC Santo André (50 vagas)
- Pátio Vila Madalena (60 vagas)
- Pátio Moema (80 vagas)

## 📊 Estrutura do Banco de Dados

O sistema utiliza **5 migrations** do Flyway:

1. **V001**: Tabela `usuarios` (autenticação OAuth2)
2. **V002**: Tabela `patios`
3. **V003**: Tabela `motos`
4. **V004**: Tabela `manutencoes`
5. **V005**: Dados iniciais (pátios e motos)


### ✅ Implementado
- ✅ **Spring Security + GitHub OAuth2**: Autenticação e autorização centralizada
- ✅ **Thymeleaf**: Layout responsivo com fragmentos
- ✅ **Flyway**: 5 migrations funcionais
- ✅ **CRUD Motos**: Completo com validações
- ✅ **CRUD Pátios**: Completo com métricas de ocupação
- ✅ **CRUD Manutenções**: Agendamento e controle completo
- ✅ **Dashboard**: Métricas por perfil (OPERADOR/MECANICO)
- ✅ **PostgreSQL + Docker**: Configuração completa

## 🔍 Endpoints da Aplicação

- **Dashboard**: `/` ou `/dashboard`
- **Motos**: `/motos`
- **Pátios**: `/patios`
- **Manutenções**: `/manutencao`
- **Mapa de Calor**: `/patios/{id}/mapa`
- **Recomendações**: `/patios/{id}/recomendacoes`

- **Rotas de Edit exemplo**: `motos/edit/{id}`
- **Rotas de Delete exemplo**: `patios/delete/{id}`

## 🌐 Deploy em Produção

A aplicação está pronta para deploy no **Render** (ou qualquer plataforma que suporte Docker).

### 🚀 Deploy Rápido no Render

1. **Push do código para GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/seu-usuario/mottu-yard.git
   git push -u origin main
   ```

2. **Criar Blueprint no Render**
   - Acesse [dashboard.render.com](https://dashboard.render.com)
   - Clique em **"New +"** → **"Blueprint"**
   - Conecte o repositório GitHub
   - O Render detectará o `render.yaml` e criará:
     - Web Service (aplicação Spring Boot)
     - PostgreSQL Database (gerenciado)

3. **Configurar variáveis de ambiente**
   - Configure as credenciais do GitHub OAuth2 no dashboard
   - As variáveis do banco são preenchidas automaticamente

4. **Atualizar URL de callback no GitHub**
   - Após deploy, atualize a URL de callback para:
     ```
     https://seu-app.onrender.com/login/oauth2/code/github
     ```

### 📖 Guia Completo de Deploy

Para instruções detalhadas passo a passo, incluindo troubleshooting e configurações avançadas, consulte:

**➡️ [DEPLOY.md](./DEPLOY.md)** - Guia completo de deploy no Render

O guia inclui:
- Configuração do GitHub OAuth App
- Deploy automatizado com `render.yaml`
- Configuração de variáveis de ambiente
- Validação e monitoramento
- Troubleshooting de problemas comuns

---

**Mottu Yard Web Application** - Sistema de Gerenciamento Inteligente de Pátio de Motos  
*Challenge FIAP - Java Advanced Sprint 4*