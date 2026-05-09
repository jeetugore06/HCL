# Credit Card Application — REST API Specification

> Z Bank hackathon. All endpoints return JSON. Errors use the uniform `ErrorResponse` envelope (§5).

**Base URL (per service):**
| Service | Base URL |
|---|---|
| customer-service | `http://localhost:8081` |
| scoring-service | `http://localhost:8082` |
| card-service | `http://localhost:8083` |

> If using an API Gateway, the single base URL hides the service split. Examples below use the service ports directly.

**Common headers:**
```
Content-Type: application/json
Accept: application/json
```

---

## 1. Apply for a Credit Card

Submits a new application. Internally publishes `customer.registered` to Kafka; the score + card decision land asynchronously and are notified by email.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/credit-cards/apply` |
| **Service** | customer-service |
| **Status code (success)** | `202 Accepted` |

### Request

```json
{
  "firstName": "Amit",
  "lastName": "Rakhaiya",
  "email": "amit@example.com",
  "phone": "+919999900000",
  "dateOfBirth": "1990-05-08",
  "annualSalary": 250000.00,
  "employerName": "HCL",
  "employmentType": "SALARIED",
  "documentType": "PAN",
  "documentId": "ABCDE1234F"
}
```

### Field validation

| Field | Rules |
|---|---|
| `firstName`, `lastName` | required, 1–50 chars |
| `email` | required, valid email format, unique |
| `phone` | required, 7–20 chars, digits/`+`/`-`/space |
| `dateOfBirth` | required, ISO date, must be in the past, age ≥ 18 |
| `annualSalary` | required, `>= 0`, scale ≤ 2 |
| `employerName` | optional, ≤ 120 chars |
| `employmentType` | required, one of: `SALARIED`, `SELF_EMPLOYED`, `BUSINESS`, `RETIRED`, `UNEMPLOYED`, `STUDENT` |
| `documentType` | required, one of: `PASSPORT`, `AADHAAR`, `PAN`, `DRIVING_LICENSE`, `VOTER_ID` |
| `documentId` | required, ≤ 40 chars; unique per `(documentType, documentId)` |

### Response — `202 Accepted`

```json
{
  "applicationReference": "f8c2b9a4-21e3-4d1f-8b6a-5e9c4f0d7b21",
  "customerReference": "9f1a3e7c-58d2-4f8b-9c2e-3d4a5b6c7e8f",
  "status": "SUBMITTED",
  "message": "Thanks for the registration. We will evaluate your application and email you.",
  "timestamp": "2026-05-09T10:15:30.123Z"
}
```

### Error responses
| HTTP | Reason |
|---|---|
| 400 | Bean Validation failure (invalid field) |
| 409 | Customer email or `(documentType, documentId)` already exists |
| 500 | Unhandled error |

---

## 2. Get Credit Score

Returns the stored credit score for a customer. **Pure read** — never computes on demand. Returns 404 if the customer hasn't been scored yet.

| | |
|---|---|
| **Method** | `GET` |
| **Path** | `/api/credit-cards/customers/{customerReference}/credit-score` |
| **Service** | scoring-service |
| **Status code (success)** | `200 OK` |

### Path params

| Param | Type | Notes |
|---|---|---|
| `customerReference` | UUID (CHAR 36) | The opaque external customer id from the apply response |

### Response — `200 OK`

```json
{
  "customerReference": "9f1a3e7c-58d2-4f8b-9c2e-3d4a5b6c7e8f",
  "score": 500,
  "createdAt": "2026-05-09T10:15:32.000Z",
  "updatedAt": "2026-05-09T10:15:32.000Z"
}
```

### Error responses
| HTTP | Reason |
|---|---|
| 400 | `customerReference` not a valid UUID |
| 404 | Customer not found, or customer found but no score row yet |

---

## 3. First-Time Login (Set / Activate PIN)

Activates the card by replacing the system-generated default PIN with a customer-chosen PIN. Allowed **only once** per card (when `pin_status = DEFAULT`). Document number is required as a second factor.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/credit-cards/first-login` |
| **Service** | card-service |
| **Status code (success)** | `200 OK` |

### Request

```json
{
  "cardNumber": "4128367812345678",
  "firstTimePin": "4321",
  "documentId": "ABCDE1234F",
  "newPin": "9876",
  "confirmNewPin": "9876"
}
```

### Field validation

| Field | Rules |
|---|---|
| `cardNumber` | required, exactly 16 digits |
| `firstTimePin` | required, exactly 4 digits |
| `documentId` | required, ≤ 40 chars |
| `newPin` | required, exactly 4 digits, must NOT equal `firstTimePin` |
| `confirmNewPin` | required, must equal `newPin` |

### Response — `200 OK`

```json
{
  "cardNumber": "************5678",
  "status": "PIN_CHANGED",
  "message": "PIN activated successfully",
  "timestamp": "2026-05-09T10:25:11.456Z"
}
```

> Card number is **masked** in the response — only the last 4 digits are returned.

### Error responses
| HTTP | Reason |
|---|---|
| 400 | Bean Validation failure, `newPin == firstTimePin`, or `newPin != confirmNewPin` |
| 404 | Card not found |
| 409 | PIN already changed (state ≠ DEFAULT), `documentId` doesn't match the card's customer, or `firstTimePin` is incorrect |

---

## 4. Change PIN (Routine)

Changes the PIN for a card that has already been activated. Requires the **current** PIN as proof of possession.

| | |
|---|---|
| **Method** | `POST` |
| **Path** | `/api/credit-cards/change-pin` |
| **Service** | card-service |
| **Status code (success)** | `200 OK` |

### Request

```json
{
  "cardNumber": "4128367812345678",
  "currentPin": "9876",
  "newPin": "1357",
  "confirmNewPin": "1357"
}
```

### Field validation

| Field | Rules |
|---|---|
| `cardNumber` | required, exactly 16 digits |
| `currentPin` | required, exactly 4 digits |
| `newPin` | required, exactly 4 digits, must NOT equal `currentPin` |
| `confirmNewPin` | required, must equal `newPin` |

### Response — `200 OK`

```json
{
  "cardNumber": "************5678",
  "status": "PIN_CHANGED",
  "message": "PIN updated successfully",
  "timestamp": "2026-05-09T10:30:22.789Z"
}
```

### Error responses
| HTTP | Reason |
|---|---|
| 400 | Bean Validation failure, or `newPin == currentPin` |
| 404 | Card not found |
| 409 | Card not yet activated (`pin_status = DEFAULT`), or `currentPin` is incorrect |

---

## 5. Uniform Error Response Envelope

All non-2xx responses use this body shape:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "PIN already changed; use /change-pin endpoint instead.",
  "timestamp": "2026-05-09T10:25:11.456Z",
  "path": "/api/credit-cards/first-login"
}
```

### Domain exception → HTTP code mapping

| Exception | HTTP | Used by |
|---|---|---|
| `MethodArgumentNotValidException` (Bean Validation) | 400 | all |
| `PinMismatchException` (`newPin != confirm`) | 400 | first-login, change-pin |
| `SamePinException` (`newPin == old`) | 400 | first-login, change-pin |
| `DuplicateCustomerException` | 409 | apply |
| `CustomerNotFoundException` | 404 | get-score |
| `CreditScoreNotFoundException` | 404 | get-score |
| `CardNotFoundException` | 404 | first-login, change-pin |
| `DocumentMismatchException` | 409 | first-login |
| `PinAlreadyChangedException` | 409 | first-login |
| `PinNotYetActivatedException` | 409 | change-pin |
| `InvalidPinException` (`firstTimePin` / `currentPin` wrong) | 409 | first-login, change-pin |
| Anything else | 500 | all |

---

## 6. Async Side Effects (Kafka)

These are **not** REST APIs but are triggered by the calls above. Documented for completeness.

| Topic | Producer | Consumer | Triggered by |
|---|---|---|---|
| `customer.registered` | customer-service | scoring-service | API #1 |
| `customer.scored` | scoring-service | card-service | scoring-service finishes |
| `card.activated` | card-service | notification-service | card issued (APPROVED) |
| `application.pending-docs` | card-service | notification-service | score = 50 |
| `application.rejected` | card-service | notification-service | score < 50 |
| `card.pin-changed` | card-service | notification-service | API #3 or #4 |

The notification-service consumes all status topics and "sends" a dummy email (logs + appends to `email-outbox.log`).

---

## 7. End-to-End Demo Flow

| # | Action | Result |
|---|---|---|
| 1 | `POST /api/credit-cards/apply` (annual salary $250,000) | `202 Accepted`, `applicationReference` returned |
| 2 | Wait ~1s for async pipeline | Kafka events flow: `customer.registered` → `customer.scored` → `card.activated` |
| 3 | Tail `email-outbox.log` | Email with PLATINUM card, $40,000 limit, card number, default PIN |
| 4 | `GET /api/credit-cards/customers/{ref}/credit-score` | `200 OK`, `score: 500` |
| 5 | `POST /api/credit-cards/first-login` with default PIN from email | `200 OK`, masked card number |
| 6 | `POST /api/credit-cards/first-login` again | `409 Conflict`, `PinAlreadyChangedException` |
| 7 | `POST /api/credit-cards/change-pin` with the PIN set in step 5 | `200 OK` |
| 8 | `POST /api/credit-cards/change-pin` with wrong current PIN | `409 Conflict`, `InvalidPinException` |

---

## 8. cURL examples

```bash
# 1. Apply
curl -X POST http://localhost:8081/api/credit-cards/apply \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Amit","lastName":"Rakhaiya",
    "email":"amit@example.com","phone":"+919999900000",
    "dateOfBirth":"1990-05-08","annualSalary":250000.00,
    "employerName":"HCL","employmentType":"SALARIED",
    "documentType":"PAN","documentId":"ABCDE1234F"
  }'

# 2. Get credit score
curl http://localhost:8082/api/credit-cards/customers/9f1a3e7c-58d2-4f8b-9c2e-3d4a5b6c7e8f/credit-score

# 3. First login
curl -X POST http://localhost:8083/api/credit-cards/first-login \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber":"4128367812345678",
    "firstTimePin":"4321","documentId":"ABCDE1234F",
    "newPin":"9876","confirmNewPin":"9876"
  }'

# 4. Change PIN
curl -X POST http://localhost:8083/api/credit-cards/change-pin \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber":"4128367812345678",
    "currentPin":"9876","newPin":"1357","confirmNewPin":"1357"
  }'
```
