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

### Delete

```bash
docker compose down -v
```

## API endpoints

### Participant

#### POST `/applications` - Create an application.

```json
{
  "directionId": 1,
  "title": "Title",
  "abstractText": "Abstract text",
  "content": "Content"
}
```

#### PUT `/applications/{id}` - Update an application.

```json
{
  "title": "Updated title",
  "abstractText": "Updated abstract text",
  "content": "Updated content"
}
```

#### POST `/applications/{id}/withdraw` - Withdraw an application.

```text
POST /applications/1/withdraw
```

### Organizer

#### GET `/organizer/applications` - List of applications with filters.

```text
GET /organizer/applications?directionId=1&status=SUBMITTED
```

#### GET `/organizer/applications/{id}` - Application in organizer's direction.

```text
GET /organizer/applications/1
```

## Tests

```bash
./gradlew test
```
