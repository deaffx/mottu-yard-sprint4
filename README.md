# Mottu Yard Web Application

Sistema de **gerenciamento inteligente do pátio de motos da Mottu** desenvolvido como projeto acadêmico para o Challenge da disciplina **Java Advanced - Sprint 4** da FIAP.

## 👥 Equipe
- **Thiago Moreno Matheus** (RM554507)
- **Celso Canaveze Teixeira Pinto** (RM556118)

## 🎯 O Desafio

A Mottu enfrenta um desafio operacional crítico: **gestão caótica de motos nos pátios**. 
Motos sem localização definida geram:
- ⏱️ Perda de tempo na busca
- 🔧 Atrasos em manutenções
- 📉 Ineficiência operacional

### Nossa Solução: Sistema de Posicionamento Inteligente

Desenvolvemos um sistema que:
- 🗺️ **Setorização automática**: Motos organizadas por status (oficina, saída rápida, espera)
- 📊 **Mapa de calor**: Visualização da ocupação em tempo real
- 🤖 **Alocação inteligente**: Recomendações automáticas de vagas
- 📋 **Histórico de movimentações**: Rastreabilidade completa

### Diferencial Competitivo
Enquanto sistemas convencionais apenas listam motos, nossa solução **otimiza o espaço 
físico e reduz o tempo de busca em até 70%**.

### 🎯 Funcionalidades Principais

- **Autenticação GitHub OAuth2**: Login usando contas autorizadas da Mottu
- **Dashboard**: Métricas em tempo real
- **Gerenciamento de Motos**: CRUD completo com validações e setorização automática
- **Gestão de Pátios**: Controle de capacidade e ocupação
- **Manutenções**: Agendamento e controle de manutenções

## 🛠️ Decisões Técnicas

### Por que Spring Boot?
Ecossistema maduro, segurança robusta via Spring Security, e facilidade de deploy em qualquer plataforma cloud.

### Por que OAuth2 com GitHub?
- ✅ **Autenticação delegada** - não gerenciamos senhas
- ✅ **Integração corporativa** facilitada
- ✅ **Segurança moderna** (padrão OAuth2)

### Por que PostgreSQL JSONB?
Permite **configuração flexível de setores por pátio** sem alterar schema. Cada pátio pode ter configuração única (oficina, saída rápida, etc.) adaptável às necessidades operacionais.

### Por que Posicionamento Inteligente?
**Diferencial competitivo** que resolve a dor específica da Mottu: organização caótica dos pátios. 

- 🗺️ Setorização automática + mapa de calor
- ⏱️ Reduz tempo de busca em até 70%
- 📊 Otimiza espaço físico com balanceamento inteligente

---

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

### Setup Rápido (local)

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

## 🔗 Repositórios Relacionados

- **DevOps Pipeline (Repositório da Sprint 3)**: [https://github.com/deaffx/mottu-yard-devops]
- **Compliance, Quality Assurance & Tests (Repositório da Sprint 3)**: [https://github.com/celsoCanaveze/mottu-yard]

- **Mobile & Backend (MOBILE APPLICATION DEVELOPMENT)**:
- Não utilizado: Integração com aplicativo mobile usando **Firebase**

## 🌐 Deploy em Produção

A aplicação está disponível para acesso em [https://mottu-yard.onrender.com].

**Mottu Yard Web Application** - Sistema de Gerenciamento Inteligente de Pátio de Motos  
*Challenge FIAP - Java Advanced Sprint 4*