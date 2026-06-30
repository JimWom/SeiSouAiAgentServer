# SeiSou AI Agent Server

Spring Boot 3 + Java 17 + MyBatis server for official-site AI chat. The server receives chat requests from the website, verifies that the visitor passed a robot check, builds agent context and skills, then forwards the request to OpenClaw.

## Current Architecture

```text
Website
  -> Controller
  -> Service
  -> DAO / OpenClaw Client
  -> Database / OpenClaw
```

Main packages:

- `controller`: HTTP API layer. It receives JSON requests and returns JSON responses.
- `dto`: API data shapes. These are request/response objects exposed to the website.
- `service`: Business flow layer. This is where robot check, context building, skill selection, and OpenClaw orchestration happen.
- `client`: External service adapter. OpenClaw calls are isolated here.
- `entity`: Database model. These classes map to database tables.
- `dao`: Database access layer. These are MyBatis mapper interfaces and contain SQL.
- `converter`: Object conversion layer. It converts between `entity` and `dto`.
- `exception`: Centralized API error handling.
- `config`: Spring configuration, CORS, OpenClaw settings, and seed data.

## Important API Endpoints

### Health

```http
GET /api/v1/health
```

### Chat

```http
POST /api/v1/chat
Content-Type: application/json

{
  "sessionId": null,
  "message": "你好，我想了解你们的服务",
  "robotCheck": {
    "token": "turnstile-token-from-frontend",
    "action": "chat"
  },
  "skillCodes": ["official-info", "lead-support"]
}
```

`sessionId` can be `null` for the first message. The response returns a `sessionId`; the website should send it back on later messages.

The website does not send `visitorId`. The backend signs an anonymous visitor id into an `HttpOnly` cookie named `seisou_visitor`.

Frontend requests must include cookies:

```ts
await fetch("http://localhost:8080/api/v1/chat", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    sessionId,
    message,
    robotCheck: {
      token: turnstileToken,
      action: "chat"
    },
    skillCodes: ["official-info", "lead-support"]
  })
});
```

### Skills

```http
GET /api/v1/skills
```

```http
POST /api/v1/skills
Content-Type: application/json

{
  "id": null,
  "code": "pricing-support",
  "name": "Pricing Support",
  "description": "Help explain pricing-related questions.",
  "instruction": "Ask what plan or usage volume the visitor is considering before answering.",
  "enabled": true
}
```

## Local Run

This project is managed by Maven. Recommended local versions:

```text
JDK: 17
Maven: 3.9.x
```

Check your environment:

```bash
java -version
mvn -version
```

Run tests:

```bash
mvn test
```

Start the server:

```bash
mvn spring-boot:run
```

Default server:

```text
http://localhost:8080
```

H2 database console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:seisou_ai_agent
```

Build a runnable jar:

```bash
mvn clean package
java -jar target/seisou-ai-agent-server.jar
```

If `mvn` is not found on Windows, install Maven or use IntelliJ IDEA's bundled Maven runner. A Maven Wrapper can be added later with:

```bash
mvn wrapper:wrapper
```

The wrapper command requires Maven once and may download wrapper files, so it is not generated in this repository until the local environment can run Maven normally.

## OpenClaw Configuration

Edit `src/main/resources/application.yml` or use environment-specific config:

```yaml
app:
  openclaw:
    base-url: https://your-openclaw-host
    api-key: your-api-key
    chat-path: /v1/chat/completions
    model: seisou-agent
```

If `base-url` is empty, the server returns a mock response. This lets frontend and backend integration start before OpenClaw is ready.

## Minimal Security

The current minimum online security set is:

- Cloudflare Turnstile server-side verification.
- CORS and backend Origin guard for official-site origins.
- Anonymous visitor cookie signed by the backend.
- `sessionId` ownership check through visitor id.
- IP and visitor rate limiting.
- Daily visitor quota.
- Per-session message quota.
- Message length validation.
- Unified JSON error responses.

For local development, `app.robot-check.enabled` is `false`.

For production:

```yaml
app:
  cors:
    allowed-origins:
      - https://your-official-site.example
  origin-guard:
    enabled: true
  visitor:
    signing-secret: replace-with-a-long-random-secret
    secure-cookie: true
  robot-check:
    enabled: true
    provider: turnstile
    turnstile-secret-key: your-cloudflare-turnstile-secret
  rate-limit:
    visitor-limit: 10
    ip-limit: 60
    window-seconds: 60
    cooldown-seconds: 120
  usage-quota:
    daily-visitor-limit: 30
    session-user-message-limit: 50
```

The frontend gets a Turnstile token, sends it as `robotCheck.token`, and the backend verifies it with Cloudflare before allowing chat.

Rate limiting is for short bursts. Usage quota is for cost control and long-running abuse. A visitor can send up to `daily-visitor-limit` successful messages per day, and a single chat session can contain up to `session-user-message-limit` user messages.

## Layer Notes

`entity` is the database table model. It should be stable and database-oriented. Example: `ChatSession`, `ChatMessage`, `SkillDefinition`.

`dto` is the API contract. It should be frontend-oriented. Example: `ChatRequest`, `ChatResponse`, `SkillDto`. Do not expose entity directly to the website, because database fields and API fields change for different reasons.

`dao` is the database access interface. In this project, DAO classes are MyBatis mapper interfaces. Example: `ChatSessionDao`, `SkillDefinitionDao`.

MyBatis often calls DAO interfaces `mapper`. This project uses the package name `dao` because it is clearer for learning: these interfaces access the database.

`converter` converts `SkillDefinition` to `SkillDto`, `ChatMessage` to `MessageDto`, and so on.

`service` contains business decisions. For this server, the important service is `ChatOrchestrationService`: robot check, session lookup, skill lookup, context build, OpenClaw call, and message persistence.

`controller` should stay thin. It should not build prompts, verify captcha, or call databases directly.

## MyBatis vs JPA

This project uses MyBatis, so SQL is written directly in DAO interfaces.

```java
@Mapper
public interface ChatSessionDao {
    @Select("select id, visitor_id, status, created_at, updated_at from chat_sessions where id = #{id}")
    ChatSession findById(UUID id);
}
```

With JPA, you usually operate Java objects and let the framework generate SQL. With MyBatis, you write SQL yourself and let MyBatis map rows to Java objects.

Short version:

```text
JPA = object first, framework generates SQL
MyBatis = SQL first, framework maps results to objects
```

For this AI chat server, MyBatis is a good fit because conversation history, skill search, admin filters, and future reports may need clear custom SQL.

## Should Context Editing Live Here Or In OpenClaw?

For now, keep context editing in this server:

- the official website can control session history and selected skills;
- robot check and visitor/session data are already here;
- changing OpenClaw later will be easier.

Move context editing into OpenClaw only if OpenClaw becomes the central agent runtime with its own memory, tools, and skill registry. A good middle path is what this project currently does: this server builds high-level context, OpenClaw handles model execution.
