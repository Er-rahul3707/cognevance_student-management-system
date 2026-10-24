# API reference

Base URL: `http://localhost:8080/api`. All request and response bodies are JSON except DELETE, which returns an empty body. Search is case-insensitive for name, email, and course and also matches the decimal student ID. Results are ordered by descending ID. The API currently has no pagination or sort parameters.

## Endpoints

| Method | Path | Request | Success | Error cases |
| --- | --- | --- | --- | --- |
| GET | `/students` | Optional `q` query parameter | 200, JSON array | 503 database unavailable |
| GET | `/students/{id}` | Positive numeric ID | 200, student object | 400 malformed ID; 404 unknown ID; 503 database unavailable |
| POST | `/students` | Student fields (below) | 201, student object | 400 invalid/malformed body; 409 duplicate email; 503 database unavailable |
| PUT | `/students/{id}` | Student fields (below) | 200, student object | 400 invalid ID/body; 404 unknown ID; 409 duplicate email |
| DELETE | `/students/{id}` | Positive numeric ID | 204, no body | 400 malformed ID; 404 unknown ID |

## Fields

| Field | Type | Rules |
| --- | --- | --- |
| `id` | integer | Server generated |
| `fullName` | string | Required, non-blank, maximum 120 characters |
| `email` | string | Required, valid email, maximum 180 characters, unique case-insensitively; normalized to lowercase |
| `phone` | string | Optional; 7–30 characters from `+()0-9 .-` when supplied |
| `course` | string | Required, non-blank, maximum 100 characters |
| `enrollmentDate` | ISO date | Required; today or earlier |
| `createdAt` | ISO timestamp | Server generated |

## Create or update example

```json
{
  "fullName": "Asha Patel",
  "email": "asha@example.com",
  "phone": "+91 98765 43210",
  "course": "Computer Science",
  "enrollmentDate": "2025-01-10"
}
```

```powershell
$body = @{ fullName = 'Asha Patel'; email = 'asha@example.com'; phone = '+91 98765 43210'; course = 'Computer Science'; enrollmentDate = '2025-01-10' } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/students' -ContentType 'application/json' -Body $body
```

A successful response contains the submitted fields plus generated values, for example:

```json
{
  "id": 1,
  "fullName": "Asha Patel",
  "email": "asha@example.com",
  "phone": "+91 98765 43210",
  "course": "Computer Science",
  "enrollmentDate": "2025-01-10",
  "createdAt": "2025-01-10T10:20:30Z"
}
```

## Search examples

- `GET /api/students?q=asha`
- `GET /api/students?q=asha%40example.com`
- `GET /api/students?q=Computer%20Science`
- `GET /api/students?q=42`

## Error contract

Errors have the shape:

```json
{
  "timestamp": "2026-10-24T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Please correct the highlighted fields",
  "fieldErrors": { "email": "must be a well-formed email address" }
}
```

Validation and malformed JSON return 400; duplicate email returns 409; unknown IDs return 404; data constraint conflicts return a generic 409; database access exceptions return a generic 503; unexpected errors return a generic 500 and are logged server-side. Responses do not include stack traces.
