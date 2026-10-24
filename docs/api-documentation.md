# API documentation

Base URL: `http://localhost:8080/api`. Full endpoint table, schema, and curl request are in the root [README](../README.md#api-reference).

## Student fields

| Field | Type | Rules |
| --- | --- | --- |
| id | integer | generated |
| fullName | string | required, max 120 |
| email | string | required, valid email, unique ignoring case, max 180 |
| phone | string | optional, 7–30 permitted phone characters |
| course | string | required, max 100 |
| enrollmentDate | date | required, today or earlier |
| createdAt | timestamp | generated |

Validation errors return HTTP 400 and field-level messages. Duplicate email returns 409; unknown student IDs return 404; deletion returns 204 with no body.

