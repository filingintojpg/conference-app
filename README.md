# Conference App

### Requirements
- Docker and Docker Compose

### Build

```bash
cp .env.example .env && docker compose up --build
```

### Run

```bash
docker compose up
```

Application: `http://localhost:8080`

### Stop

```bash
docker compose down
```

## API endpoints

### POST `/applications`

Create an application.

```json
{
  "directionId": 1,
  "title": "Title",
  "abstractText": "Application abstract",
  "content": "Application content"
}
```

### PUT `/applications/{id}`

Update an application before T1.

```json
{
  "title": "Updated title",
  "abstractText": "Updated abstract",
  "content": "Updated content"
}
```

### POST `/applications/{id}/withdraw`

Withdraw an application.

```text
POST /applications/1/withdraw
```

## Tests

```bash
./gradlew test
```

HTML report: `build/reports/tests/test/index.html`.
