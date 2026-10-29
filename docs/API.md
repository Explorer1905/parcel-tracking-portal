# API Reference – Parcel Tracking Portal

**Base URL (local):** `http://localhost:8090/parcel-tracking-portal`  
**Content-Type:** All API responses use `application/json;charset=UTF-8`  
**Authentication:** None (all endpoints are currently public)

---

## GET /health

Health probe. Used by Docker Compose healthcheck and Ansible `health-check` role.

**Response 200:**
```
OK
```

---

## GET /api/summary

Returns dashboard counts.

**Response 200:**
```json
{
  "total": 18,
  "inTransit": 6,
  "delivered": 2,
  "delayed": 10
}
```

**Delayed rule:** latest status is `Failed`, OR not `Delivered` and silent > 48 hours.

---

## GET /api/parcels

Returns a paginated list of parcels. Supports fuzzy search across parcel ID, sender, receiver, origin and destination.

**Query parameters:**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `q` | string | `""` | Case-insensitive LIKE search |
| `status` | string | `""` | Filter by exact status |
| `page` | int | 1 | Page number (1-based) |
| `size` | int | 10 | Page size (1–100) |

**Response 200:**
```json
{
  "total": 18,
  "page": 1,
  "size": 2,
  "parcels": [
    {
      "id": "DEMO-1001",
      "sender": "Asha Traders",
      "receiver": "Rohan Mehta",
      "origin": "Mumbai",
      "destination": "Pune",
      "status": "Delivered",
      "priority": "Express",
      "weightKg": 2.5,
      "expectedDelivery": "2026-10-30",
      "assignedAgent": "Ravi Kumar",
      "createdAt": "2026-10-23 14:52:13.934709"
    }
  ]
}
```

**Example:**
```bash
curl "http://localhost:8090/parcel-tracking-portal/api/parcels?q=Mumbai&page=1&size=5"
```

---

## GET /api/parcels/{id}

Returns a single parcel with its full event timeline.

**Path parameter:** `id` — parcel ID (case-insensitive lookup)

**Response 200:**
```json
{
  "id": "DEMO-1001",
  "sender": "Asha Traders",
  "receiver": "Rohan Mehta",
  "origin": "Mumbai",
  "destination": "Pune",
  "status": "Delivered",
  "priority": "Express",
  "weightKg": 2.5,
  "expectedDelivery": "2026-10-30",
  "assignedAgent": "Ravi Kumar",
  "createdAt": "2026-10-23 14:52:13.934709",
  "events": [
    {
      "id": 1,
      "status": "Delivered",
      "location": "Pune",
      "eventTime": "2026-10-23 19:52:13.934709",
      "remarks": "Signed by receiver"
    }
  ]
}
```

**Response 404:**
```json
{ "error": "Parcel \"DEMO-9999\" not found" }
```

**Example:**
```bash
curl http://localhost:8090/parcel-tracking-portal/api/parcels/DEMO-1001
```

---

## POST /api/parcels

Creates a new parcel. All validation runs before any database call.

**Request body (JSON):**
```json
{
  "id": "API-001",
  "sender": "Test Sender",
  "receiver": "Test Receiver",
  "origin": "Delhi",
  "destination": "Chennai",
  "weight": "3.5",
  "priority": "Express",
  "expectedDelivery": "2026-11-15",
  "assignedAgent": "Ravi Kumar"
}
```

**Required fields:** `id`, `sender`, `receiver`, `origin`, `destination`  
**Optional fields:** `weight` (default 1.0 kg), `priority` (default Standard), `expectedDelivery`, `assignedAgent`

**Validation rules:**

| Field | Rule |
|-------|------|
| `id` | 3–30 chars, letters / digits / hyphen only |
| `sender`, `receiver`, `origin`, `destination` | ≤ 100 chars, required |
| `weight` | Decimal, 0.01–1000 kg |
| `priority` | `Standard`, `Express`, or `Urgent` |
| `expectedDelivery` | ISO date `yyyy-MM-dd`, not before today |
| `assignedAgent` | ≤ 100 chars |

**Response 201** — returns the created parcel:
```json
{
  "id": "API-001",
  "sender": "Test Sender",
  ...
  "status": "Registered"
}
```

**Response 400:**
```json
{ "error": "Parcel ID must be 3 to 30 characters: letters, digits or hyphen." }
```

**Example:**
```bash
curl -X POST http://localhost:8090/parcel-tracking-portal/api/parcels \
  -H "Content-Type: application/json" \
  -d '{"id":"API-001","sender":"Alice","receiver":"Bob","origin":"Mumbai","destination":"Pune","weight":"2.5","priority":"Express"}'
```

---

## POST /api/parcels/{id}/events

Adds a tracking event to an existing parcel.

**Request body (JSON):**
```json
{
  "status": "In Transit",
  "location": "Lonavala",
  "eventTime": "2026-10-29T12:00",
  "remarks": "Left sorting hub"
}
```

**Valid statuses:** `Picked Up`, `In Transit`, `Out for Delivery`, `Delivered`, `Failed`

**Response 201:**
```json
{ "message": "Event added" }
```

**Response 400:**
```json
{ "error": "Choose a valid status." }
```

**Example:**
```bash
curl -X POST http://localhost:8090/parcel-tracking-portal/api/parcels/API-001/events \
  -H "Content-Type: application/json" \
  -d '{"status":"In Transit","location":"Lonavala","remarks":"Left hub"}'
```

---

## GET /export/parcels.csv

Exports all parcels as a downloadable CSV file.

**Response headers:**
```
Content-Type: text/csv;charset=UTF-8
Content-Disposition: attachment; filename="parcels.csv"
```

**CSV columns:**
```
Parcel ID, Sender, Receiver, Origin, Destination, Status, Priority,
Weight (kg), Expected Delivery, Assigned Agent, Created At
```

**Example:**
```bash
curl -o parcels.csv http://localhost:8090/parcel-tracking-portal/export/parcels.csv
```

---

## GET /track?id={parcelId}

Public HTML tracking page. Returns parcel status, route, priority, and full event timeline. No authentication required.

**Parameters:** `id` — parcel ID to look up  
**Response:** HTML page with status badge, route, expected delivery, event timeline table

---

## Error Response Format

All API error responses use the same structure:
```json
{ "error": "<human-readable message>" }
```

| HTTP Status | Meaning |
|-------------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Validation error |
| 404 | Parcel not found |
| 500 | Server error (check Docker logs) |

---

## Data Model

```
Parcel
  id              VARCHAR(30) PK
  sender          VARCHAR(100) NOT NULL
  receiver        VARCHAR(100) NOT NULL
  origin          VARCHAR(100) NOT NULL
  destination     VARCHAR(100) NOT NULL
  weight_kg       NUMERIC(8,2) DEFAULT 1.0  CHECK (0.01–1000)
  priority        VARCHAR(10)  DEFAULT 'Standard' CHECK (Standard|Express|Urgent)
  expected_delivery DATE
  assigned_agent  VARCHAR(100)
  created_at      TIMESTAMP DEFAULT LOCALTIMESTAMP

ParcelEvent
  event_id    BIGSERIAL PK
  parcel_id   VARCHAR(30) FK→parcels
  status      VARCHAR(30)  CHECK (Registered|Picked Up|In Transit|Out for Delivery|Delivered|Failed)
  location    VARCHAR(100)
  event_time  TIMESTAMP DEFAULT LOCALTIMESTAMP
  remarks     VARCHAR(200)
```
