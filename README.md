# Conference App

## Requirements
- Docker and Docker Compose

## Run instruction

### Build

```bash
cp .env.example .env && docker compose up --build
```

### Run

```bash
docker compose up
```

Backend: `http://localhost:8080`, frontend: `http://localhost:5173`

### Stop

```bash
docker compose down
```

### Delete

```bash
docker compose down -v
```

## API endpoints

### Common

#### GET `/directions` - List of directions.

```text
GET /directions
```

#### GET `/users` - List of users (optional role filter).

```text
GET /users?role=ORGANIZER
```

#### GET `/application-statuses` - List of possible application statuses.

```text
GET /application-statuses
```

#### GET `/review-statuses` - List of possible review statuses.

```text
GET /review-statuses
```

### Participant

#### GET `/applications` - List of the participant's applications.

```text
GET /applications
```

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
GET /organizer/applications?directionId=1&status=SUBMITTED&reviewStatus=PENDING
```

#### GET `/organizer/applications/{id}` - Application in organizer's direction.

```text
GET /organizer/applications/1
```

#### POST `/organizer/applications/{id}/approve` - Application passed the review.

```text
POST /organizer/applications/1/approve
```

#### POST `/organizer/applications/{id}/reject` - Application did not pass the review.

```text
POST /organizer/applications/1/reject
```

## Tests

```bash
./gradlew test
```
