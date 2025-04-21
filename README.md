# 🎬 Flix Wrapped API

API desenvolvida em [Micronaut](https://micronaut.io/) para processar e gerar insights personalizados com base no histórico de filmes e séries assistidos. Inspirado no Spotify Wrapped, mas para conteúdo audiovisual.

## 🚀 Funcionalidades

- Upload de histórico via CSV exportado da Netflix
- Armazenamento temporário dos dados (auto-expiração após 7 dias)
- Processamento e classificação de filmes/séries
- Geração de insights: séries mais assistidas, consumo por ano, últimos assistidos e mais
- Integração com OMDb para metadados dos títulos
- Autenticação via Google OAuth

## 🧪 Tecnologias

- Micronaut
- MongoDB
- Java 21
- OMDb API
- Google OAuth
- Caffeine Cache
- Swagger (RapiDoc)

## 🛠️ Executando localmente

### Pré-requisitos

- Java 21
- MongoDB Atlas ou local
- OMDb API Key
- Google OAuth Client ID e Secret

### Configuração

Crie um arquivo `application-local.yml` (não versionado) com:

```yaml
mongodb:
  uri: YOUR_MONGODB_URI

omdb:
  apikey: YOUR_OMDB_API_KEY

auth:
  clientId: YOUR_GOOGLE_CLIENT_ID
  secretKey: YOUR_GOOGLE_SECRET_KEY

frontend:
  url: http://localhost:5173
