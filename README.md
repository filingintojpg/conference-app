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

#### GET `/users` - List of users (optional role filter).

```text
GET /users?role=ORGANIZER
```

#### GET `/application-statuses` - List of possible application statuses.

#### GET `/review-statuses` - List of possible review statuses.

### Participant

#### GET `/applications` - List of the participant's applications.

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

### Organizer

#### GET `/organizer/applications` - List of applications with filters.

```text
GET /organizer/applications?directionId=1&status=SUBMITTED&reviewStatus=PENDING
```

#### GET `/organizer/applications/{id}` - Application in organizer's direction.

#### POST `/organizer/applications/{id}/approve` - Application passed the review.

#### POST `/organizer/applications/{id}/reject` - Application did not pass the review.

### Expert committee member

#### GET `/expert/applications` - Approver applications without author data.

```text
GET /expert/applications?directionId=1
```

## Tests

```bash
./gradlew test
```
