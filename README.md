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

### Auth

#### POST `/auth/register` - Register a participant and sign in.

```json
{
  "lastName": "Ivanov",
  "firstName": "Ivan",
  "middleName": "Ivanovich",
  "email": "new.user@example.com",
  "password": "at-least-8-chars"
}
```

#### POST `/auth/login` - Sign in.

```json
{
  "email": "ivan@example.com",
  "password": "password123"
}
```

#### POST `/auth/logout` - Sign out.

#### GET `/auth/me` - Current user by token.

### Common

#### GET `/directions` - List of directions.

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

#### POST `/organizer/applications/{id}/transfer` - Transfer an unreviewed application to another direction.

```json
{
  "targetDirectionId": 2
}
```

#### POST `/organizer/applications/{id}/transfer/accept` - Accept the application into direction.

#### POST `/organizer/applications/{id}/transfer/reject` - Decline the transfer, the application stays in the source direction.

### Expert committee member

#### GET `/expert/applications` - Approved applications of the expert's direction, without author data.
## Tests

```bash
./gradlew test
```
