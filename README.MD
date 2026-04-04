# PulseDesk

> AI-powered support ticketing. From raw user comments to structured tickets in seconds.

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen)
![Docker](https://img.shields.io/badge/Docker-ready-blue)
![License](https://img.shields.io/badge/license-MIT-green)

PulseDesk is a full-stack AI-powered ticketing system that ingests user comments from multiple source channels, analyzes their intent and sentiment using Hugging Face models, and automatically generates prioritized support tickets. It is designed for support teams that want to eliminate manual ticket triage and reduce response time.

**Live app:** [https://pulsedesk-production-56ff.up.railway.app](https://pulsedesk-production-56ff.up.railway.app)

---

## 📑 Table of Contents

- [Features](#-features)
- [Project Structure](#️-project-structure)
- [Getting Started](#-getting-started)
- [Deployment](#-deployment)
- [API Reference](#-api-reference)
- [Roadmap](#️-roadmap)
- [Disclaimer](#-disclaimer)
- [Acknowledgements](#-acknowledgements)
- [License](#-license)

---

## ✨ Features

- **AI-driven ticket generation.** Submitting a comment automatically triggers a Hugging Face model that classifies intent and creates a ticket.
- **Automatic priority assignment.** Billing, access, and critical issues are detected and labelled `HIGH`, `MEDIUM`, or `LOW` without manual input.
- **Multi-channel ingestion.** Accepts comments from `APP_REVIEW`, `CHAT_WIDGET`, and `OTHER` source channels.
- **Paginated REST API.** All list endpoints support `page`, `size`, and `sort` query parameters.
- **Swagger UI.** Interactive API docs available out of the box at `/swagger-ui.html`.
- **Docker ready.** Ships with a multi-stage Dockerfile that builds and runs the full app in a single container.

---

## 🗂️ Project Structure

```
pulsedesk-ai-ticketing/
├── pulsedesk-ui/               # TypeScript frontend dashboard
│   └── src/                    # Components, pages, services
├── src/main/java/org/pulsedesk/
│   ├── ai/                     # Hugging Face client and analysis logic
│   ├── comment/                # Comment entity, repository, service, controller
│   ├── ticket/                 # Ticket entity, repository, service, controller
│   ├── config/                 # HTTP client configuration
│   └── exception/              # Global exception handling
├── src/main/resources/
│   ├── static/                 # Built frontend files served by Spring Boot
│   └── application.properties  # App configuration
├── .env.example                # Environment variable template
├── Dockerfile                  # Multi-stage Docker build
└── pom.xml                     # Maven dependencies and build config
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 25 |
| Maven | 3.9+ or use the included `mvnw` wrapper |
| Node.js | 22.12+ |
| Hugging Face account | Required for an API token |

### Installation

1. Clone the repository.

```bash
git clone https://github.com/Yusufgoobrr/pulsedesk-ai-ticketing.git
cd pulsedesk-ai-ticketing
```

2. Create your `.env` file from the template and add your token.

```bash
cp .env.example .env
```

3. Run the application. See the [Deployment](#-deployment) section for all available run options.

---

## 🌍 Deployment

The app is live at **[https://pulsedesk-production-56ff.up.railway.app](https://pulsedesk-production-56ff.up.railway.app)**, hosted on Railway.

You can also run it yourself using any of the three options below. All options require a Hugging Face API token passed as `HF_TOKEN`.

#### Option 1. Docker Hub

Pull and run the published image from [`yusufgoobrr/pulsedesk`](https://hub.docker.com/r/yusufgoobrr/pulsedesk).

```bash
docker run -p 8080:8080 --env HF_TOKEN=your_token_here yusufgoobrr/pulsedesk
```

#### Option 2. Build and run the JAR

```bash
./mvnw clean package -DskipTests
```

On Unix:

```bash
HF_TOKEN=your_token_here java -jar target/pulsedesk-0.0.1-SNAPSHOT.jar
```

On Windows:

```cmd
set HF_TOKEN=your_token_here
java -jar target/pulsedesk-0.0.1-SNAPSHOT.jar
```

#### Option 3. Run from source

```bash
./mvnw spring-boot:run
```

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

Make sure your `.env` file has `HF_TOKEN` set before running.

All options serve the app at `http://localhost:8080`.

---

## 📖 API Reference

### Comments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/comments` | List all comments (paginated). |
| `POST` | `/api/v1/comments` | Submit a comment and trigger AI ticket generation. |

**POST `/api/v1/comments` request body**

| Field | Type | Description |
|-------|------|-------------|
| `comment` | `string` | The user comment text to analyse. |
| `author` | `string` | Author name or identifier. |
| `sourceChannel` | `string` | One of `APP_REVIEW`, `CHAT_WIDGET`, `OTHER`. |

### Tickets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/tickets` | List all generated tickets (paginated). |
| `GET` | `/api/v1/tickets/{id}` | Retrieve a single ticket by ID. |

All list endpoints accept `page`, `size`, and `sort` query parameters.

---

## 🗺️ Roadmap

- [ ] Persist data with PostgreSQL or MySQL.
- [ ] Add database migrations with Flyway.
- [ ] Add ticket status transitions (`OPEN` → `IN_PROGRESS` → `RESOLVED`).
- [ ] Support ticket assignment to specific agents.
- [ ] Add authentication and role-based access control.

---

## 🤖 Disclaimer

The `pulsedesk-ui` frontend was built with the assistance of AI through vibe coding. It is functional and serves as a dashboard for the backend API, but may not follow all conventional frontend best practices. Contributions are welcome.

---

## 🙏 Acknowledgements

- [Spring Boot](https://spring.io/projects/spring-boot) — application framework.
- [Hugging Face](https://huggingface.co) — inference API and NLP models.
- [SpringDoc OpenAPI](https://springdoc.org) — automatic Swagger UI generation.
- [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) — `.env` file integration for Spring Boot.
- [H2 Database](https://h2database.com) — embedded SQL database for development.

---

## 📄 License

This project is licensed under the [MIT License](./LICENSE). Free for personal and commercial use.