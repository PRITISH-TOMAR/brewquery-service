# BrewQuery — Backend Architecture

## Overview

The backend is split into two Spring Boot services that communicate through a combination of **direct HTTP** and a **Redis queue**, depending on the operation type.

```
                        ┌─────────────────────────────────┐
                        │          sqlbrew-app             │
                        │         (React frontend)         │
                        └────────────┬────────────────────┘
                                     │ HTTP (Axios + JWT)
                        ┌────────────▼────────────────────┐
                        │           services               │
                        │    (club.sqlhub — port 8080)     │
                        │                                  │
                        │  Auth, Problem/Dataset metadata  │
                        │  Submission orchestration        │
                        │  Result polling + persistence    │
                        └──────┬──────────────┬───────────┘
                     WebClient │              │ Redis
                   (sync/Run)  │              │ (async/Submit)
                        ┌──────▼──────────────▼───────────┐
                        │            engine                │
                        │  (in.brewquery_engine — 8080)    │
                        │                                  │
                        │  SQL execution, test case judge  │
                        │  In-memory H2, session mgmt      │
                        └─────────────────────────────────┘
                                     │
                              Shared Redis
                         (sessions, queue, results)
```

---

## Two Communication Paths

### 1. Run — Synchronous HTTP

Used when the user clicks **Run**. Executes only the **public** test cases and returns immediately.

```
Frontend  POST /sql/run  { questionId, query }
    │
    ▼
services: SQLRemoteService.runQuery()
    │  1. Fetch Question from MongoDB (get datasetId, queryType)
    │  2. Fetch PUBLIC test cases from MongoDB
    │  3. Fetch expected SQL for the question
    │  4. Build JudgeJobPayload { jobId, type:"SQL", payload: SQLPayload as JSON }
    │  5. SQLRemoteRepository.runPublicTestCases(payload)
    │       └─ SQLRemoteApiHelper.post(judge.run-testcases-url, payload)
    │              └─ WebClient → POST engine /jobs/test
    │
    ▼
engine: JobsController POST /jobs/test
    │  JobsService.runPublicTestCases()
    │  1. Parse SQLPayload from payload string
    │  2. SafeQueryValidator — reject unsafe SQL
    │  3. Filter test cases: type == "public" only
    │  4. TestCaseExecutor.execute() for each → RunTestcaseResponseDTO
    │  5. Return synchronously
    │
    ▼
services → frontend: RunTestcaseResponseDTO { passedCount, totalCount, totalExecutionMs, overallStatus, testDetails }
```

---

### 2. Submit — Async via Redis Queue

Used when the user clicks **Submit**. Runs **all** test cases (public + private) asynchronously. Services returns a `jobId` immediately; the frontend polls for the result.

#### Phase A — Enqueue (services)

```
Frontend  POST /sql/execute  { questionId, query }
    │
    ▼
services: SQLRemoteService.executeQuery()
    │  1. Fetch Question from MongoDB
    │  2. Fetch ALL test cases from MongoDB
    │  3. Fetch expected SQL
    │  4. Generate jobId = SHA256( SHA256(userId) + ":" + datasetId + ":" + timestamp ) → URL-safe Base64
    │  5. Build JudgeJobPayload { jobId, type:"SQL", userId, payload: SQLPayload as JSON }
    │  6. Redis LPUSH  "judge:queue:sql"  →  jobPayload JSON       (enqueue)
    │  7. Redis SET    "meta:sql:{jobId}" → userId  (TTL 1 hour)   (for history persistence)
    │  8. Return immediately: { jobId, status: "QUEUED" }
    │
    ▼
Frontend receives jobId, begins polling GET /result/{jobId} every 1.5 s (max 40 attempts / ~60 s)
```

#### Phase B — Process (engine)

```
engine: JobWorker  (daemon thread, started via CommandLineRunner on boot)
    │
    │  Loop forever:
    │    Redis BRPOP "judge:queue:sql" (blocking, 5 s timeout — FIFO)
    │    if nothing → loop again
    │
    │  On job received:
    │  1. Deserialize JudgeJobPayload
    │  2. JobsService.processSubmission(payload)
    │       a. Parse SQLPayload
    │       b. SafeQueryValidator — abort if unsafe
    │       c. TestCaseExecutor.execute() for EVERY test case
    │       d. Build RunTestcaseResponseDTO { passedCount, totalCount, totalExecutionMs, overallStatus, testDetails }
    │  3. Redis SET  "result:sql:{jobId}"  →  result JSON  (TTL 1 hour)
```

#### Phase C — Poll (services)

```
Frontend  GET /result/{jobId}
    │
    ▼
services: JudgeService.expectedOutput(jobId)
    │
    │  1. Redis GET "result:sql:{jobId}"
    │       found  → deserialize RunTestcaseResponseDTO
    │                → persist to MongoDB (UserQueriesResult, keyed by jobId — upsert)
    │                → map verdict (see below)
    │                → return SubmissionResponseDTO
    │
    │  2. Not in Redis (TTL expired) → MongoDB lookup by jobId
    │       found  → convert + return SubmissionResponseDTO
    │
    │  3. Neither → return { verdict: "PENDING" }   (engine still processing)
```

---

## Verdict Mapping

| Engine `overallStatus` | Frontend verdict |
|------------------------|-----------------|
| `PASS` | `ACCEPTED` |
| `FAIL` | `WRONG_ANSWER` |
| `PARTIAL` | `PARTIAL_ACCEPTED` |
| anything else | passed through as-is |

---

## Test Case Execution (Engine)

Every test case — whether from Run or Submit — goes through the same `TestCaseExecutor`. Each gets its own **ephemeral, isolated in-memory H2 connection** that is created, used, and closed per test case:

```
TestCaseExecutor.execute(tc, userSql, expectedSql, sqlMode)
    │
    │  1. EphemeralConnectionFactory.create(tc.id, sqlMode)  → new H2 Connection
    │  2. SqlBatchExecutor.run(conn, tc.schemaSql)            → create tables
    │  3. SqlBatchExecutor.run(conn, tc.seedSql)              → insert seed data
    │  4. SQLExecutor.executeQuery(conn, userSql)             → SQLQueryResponseDTO (user)
    │  5. SQLExecutor.executeQuery(conn, expectedSql)         → SQLQueryResponseDTO (expected)
    │  6. ResultComparator.compare(user, expected, tolerance) → pass/fail
    │  7. EphemeralConnectionFactory.safeClose(conn)
    │
    └─ returns TestCaseResult { testCaseId, userOutput, expectedOutput, passed, error? }
```

### Result Comparison Rules (`ResultComparator`)

- Column sets must match (unordered)
- Row counts must match
- Row values compared after **sorting both result sets** → order-insensitive by default
- Numeric values support an optional **tolerance** per test case (e.g. `0.01` for floating point)
- All other values compared as strings

---

## Shared Redis Key Space

| Key pattern | Written by | Read by | TTL | Purpose |
|---|---|---|---|---|
| `judge:queue:sql` | services | engine (BRPOP) | none (queue) | Async submission queue |
| `meta:sql:{jobId}` | services | services | 1 hour | Map jobId → userId for history |
| `result:sql:{jobId}` | engine | services | 1 hour | Submission result |
| `session:{sessionId}` | engine | engine | configurable | SQL session TTL tracking |

---

## Engine Session vs. Ephemeral Connection

The engine has two separate connection models that serve different purposes:

| | Session Connection | Ephemeral Connection |
|---|---|---|
| Used for | `POST /engine/sql/load` + `/execute` (interactive queries) | Test case judging (Run/Submit) |
| Lifetime | Persists across requests; TTL managed via Redis | Created and destroyed per test case |
| Stored in | `SessionManager.activeSessions` (ConcurrentHashMap) | Local variable in `TestCaseExecutor` |
| Data loaded | Dataset schema + inserts once on load | Test case schema + seed SQL each time |
| Cleanup | Hourly GC + startup flush | `safeClose()` in finally block |

---

## HTTP Transport (services → engine, synchronous path)

`SQLRemoteApiHelper` wraps Spring **WebClient** (reactive, blocking call via `.block()`). It handles three response shapes gracefully:

1. Standard `ApiResponse<T>` envelope
2. Generic JSON (extracts `message` / `error` fields)
3. Plain text

Engine URLs are injected via config properties:
- `judge.run-testcases-url` → `POST /jobs/test`  (Run)
- `judge.execute-url` → `POST /jobs`  (direct submit, not used in async path)
