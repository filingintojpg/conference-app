# Conference App

### Requirements
- Docker and Docker Compose

### Build

```bash
cp .env.example .env && docker compose up --build
```

Application: `http://localhost:8080`

### Stop

```bash
docker compose down
```

## API endpoints

### POST `/application/create`

Create an application.

```json
{
  "participantId": 1,
  "directionId": 1,
  "title": "Spring Boot",
  "abstractText": "Application abstract",
  "content": "Application content"
}
```

### PUT `/application/{id}`

Update an application before T1.

```json
{
  "title": "Updated Spring Boot",
  "abstractText": "Updated abstract",
  "content": "Updated content"
}
```

### POST `/application/{id}/withdraw`

Withdraw an application before T1.

```text
POST /application/1/withdraw
```

## Postman

Import:

```text
postman-collections/iteration_1.json
```

Set `participantId` and `directionId` to existing database records before creating an application.

## Database migrations

Migrations are stored in:

```text
src/main/resources/db/migration/
```

Do not edit an already applied migration. Add a new migration instead.
