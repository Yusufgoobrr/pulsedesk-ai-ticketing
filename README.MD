# PulseDesk

> AI-powered support ticketing — from raw user comments to structured tickets in seconds.

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen)
![License](https://img.shields.io/badge/license-MIT-green)

PulseDesk is a full-stack AI-powered ticketing system that ingests user comments from multiple source channels, analyzes their intent and sentiment using Hugging Face models, and automatically generates prioritized support tickets. It is designed for support teams that want to eliminate manual ticket triage and reduce response time. The system ships with a Spring Boot REST backend, an H2 in-memory database, Swagger UI documentation, and a TypeScript frontend dashboard.

---

## 📑 Table of Contents

- [Features](#-features)
- [Getting Started](#-getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Project Structure](#️-project-structure)
    - [Environment Variables](#environment-variables)
- [Usage](#-usage)
- [API Reference](#-api-reference)
- [Roadmap](#️-roadmap)
- [Contributing](#-contributing)
- [Disclaimer](#-disclaimer)
- [License](#-license)
- [Acknowledgements](#-acknowledgements)

---

## ✨ Features

- **AI-driven ticket generation** — submitting a comment automatically triggers a Hugging Face model that classifies intent and creates a ticket.
- **Automatic priority assignment** — billing, access, and critical issues are detected and labelled `HIGH`, `MEDIUM`, or `LOW` without manual input.
- **Multi-channel ingestion** — accepts comments from `APP_REVIEW`, `CHAT_WIDGET`, and `OTHER` source channels in a single unified API.
- **Paginated REST API** — all list endpoints support `page`, `size`, and `sort` query parameters.
- **Interactive API docs** — Swagger UI available out of the box via SpringDoc OpenAPI.
- **H2 console** — built-in browser-based database console for quick inspection during development.
- **TypeScript frontend** — a `pulsedesk-ui` dashboard for browsing comments and tickets without touching the API directly.
- **Dotenv support** — secrets loaded from a `.env` file; no manual `application.properties` edits needed.

---

## 🚀 Getting Started

### Prerequisites

| Tool | Minimum version |
|------|----------------|
| Java (JDK) | 25 |
| Maven | 3.9+ (or use the included `mvnw` wrapper) |
| Node.js | 18+ (frontend only) |
| npm | 9+ (frontend only) |
| Hugging Face account | — (for an API token) |

### Installation

#### Backend

1. Clone the repository.

```bash
git clone https://github.com/Yusufgoobrr/pulsedesk-ai-ticketing.git
cd pulsedesk-ai-ticketing
```

2. Copy the environment variable template and fill in your token.

```bash
cp .env.example .env
# Open .env and set HF_TOKEN=<your_hugging_face_token>
```

3. Build and run the Spring Boot application.

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8080` by default.

4. Open the interactive API docs in your browser: `http://localhost:8080/swagger-ui.html`

5. (Optional) Open the H2 database console: `http://localhost:8080/h2-console`

#### Frontend

1. Navigate to the frontend directory.

```bash
cd pulsedesk-ui
```

2. Install dependencies.

```bash
npm install
```

3. Start the development server.

```bash
npm run dev
```

The UI is served at `http://localhost:5173` (or the port shown in your terminal).

### 🗂️ Project Structure

```
pulsedesk-ai-ticketing/
├── .mvn/wrapper/            # Maven wrapper metadata and JAR
├── pulsedesk-ui/            # TypeScript frontend dashboard
│   ├── src/                 #   Application source (components, pages, services)
│   └── package.json         #   Frontend dependencies and scripts
├── src/
│   └── main/
│       ├── java/com/yusufkaya/pulsedesk/
│       │   ├── comment/     #   Comment entity, repository, service, controller
│       │   ├── ticket/      #   Ticket entity, repository, service, controller
│       │   └── ai/          #   Hugging Face client and analysis logic
│       └── resources/
│           └── application.properties  # Spring Boot configuration
├── .env.example             # Environment variable template
├── .gitignore
├── mvnw / mvnw.cmd          # Maven wrapper scripts (Unix / Windows)
├── pom.xml                  # Maven build descriptor and dependency list
└── requests.http            # Sample HTTP requests for IDE REST clients
```

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `HF_TOKEN` | **Yes** | — | Hugging Face API token for comment analysis. Get one at [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens). |

---

## 💡 Usage

### Submit a comment and auto-generate a ticket

Post a comment from any source channel. PulseDesk analyses the text and creates a ticket automatically.

```bash
curl -X POST http://localhost:8080/api/v1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "I cannot log into my account, it keeps failing.",
    "author": "alice",
    "sourceChannel": "APP_REVIEW"
  }'
```

### Submit a billing complaint (auto-assigned HIGH priority)

Comments about charges or billing are classified as high-priority tickets.

```bash
curl -X POST http://localhost:8080/api/v1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "I was charged twice for my subscription.",
    "author": "bob",
    "sourceChannel": "OTHER"
  }'
```

### Retrieve all tickets (paginated, newest first)

```bash
curl "http://localhost:8080/api/v1/tickets?page=0&size=10&sort=id,desc"
```

### Fetch a specific ticket by ID

```bash
curl http://localhost:8080/api/v1/tickets/1
```

---

## 📖 API Reference

### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/comments` | List all comments (paginated). |
| `POST` | `/api/v1/comments` | Create a comment and trigger AI ticket generation. |

**POST `/api/v1/comments` — request body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `comment` | `string` | Yes | The raw user comment text to analyse. |
| `author` | `string` | Yes | Display name or identifier of the comment author. |
| `sourceChannel` | `string` | Yes | Origin of the comment. One of `APP_REVIEW`, `CHAT_WIDGET`, `OTHER`. |

### Tickets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/tickets` | List all generated tickets (paginated). |
| `GET` | `/api/v1/tickets/{id}` | Retrieve a single ticket by its ID. |

### Pagination query parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | `integer` | `0` | Zero-based page number. |
| `size` | `integer` | `20` | Number of items per page. |
| `sort` | `string` | `id,asc` | Sort field and direction, e.g. `id,desc`. |

> The full interactive reference is available at `http://localhost:8080/swagger-ui.html` when the backend is running.

---

## 🗺️ Roadmap

- [ ] Persist data with PostgreSQL / MySQL in addition to H2.
- [ ] Add ticket status transitions (`OPEN` → `IN_PROGRESS` → `RESOLVED`).
- [ ] Support ticket assignment to specific agents.
- [ ] Add authentication and role-based access control.
- [ ] [WIP] Dockerize backend and frontend with `docker-compose`.
- [ ] Webhook notifications on ticket creation.
- [ ] Support additional Hugging Face models via configuration.

---

## 🤝 Contributing

Contributions are welcome! Please follow the steps below.

1. **Fork** the repository on GitHub.
2. **Create a branch** for your feature or fix.

```bash
git checkout -b feat/your-feature-name
```

3. **Commit** your changes with a descriptive message.

```bash
git commit -m "feat: add ticket assignment endpoint"
```

4. **Push** the branch and open a **Pull Request** against `main`.

```bash
git push origin feat/your-feature-name
```

**Code standards**

- Follow standard Java conventions (Google Java Style or the existing code style in the project).
- All new backend features should include unit tests written with JUnit 5 and Mockito.
- Run `./mvnw test` before submitting a PR and ensure all tests pass.
- Frontend changes should be linted with `npm run lint` (if configured).

No CLA is currently required.

---

## 🤖 Disclaimer

The `pulsedesk-ui` frontend was built with the assistance of AI through vibe coding. It is functional and serves as a dashboard for the backend API, but the code may not follow all conventional frontend best practices. Contributions and improvements are very welcome!

---

## 📄 License

This project is licensed under the [MIT License](./LICENSE) — free for personal and commercial use.

---

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot) — application framework.
- [Hugging Face](https://huggingface.co) — inference API and NLP models.
- [SpringDoc OpenAPI](https://springdoc.org) — automatic Swagger UI generation.
- [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) — `.env` file integration for Spring Boot.
- [H2 Database](https://h2database.com) — embedded SQL database for development.