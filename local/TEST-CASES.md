# API test cases (local stack)

Reproducible scenarios for `GET /api/v1/transactions`. Expected values below assume the
**stub** FX profile (fixed rate **1.10** for any cross-currency pair; same-currency = 1.0),
which makes outputs deterministic. With the `real-fx` profile the shape is identical but the
converted amounts use the **live** rate (see the last section).

## Setup
```powershell
docker compose up -d --build app          # default = stub profile
# wait until healthy, then seed once:
docker cp local/sample-transactions.jsonl kafka:/tmp/seed.jsonl
docker compose exec -T kafka sh -c '/opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic transactions < /tmp/seed.jsonl'
# token for Swagger / Authorization header:
(Invoke-RestMethod -Method Post "http://localhost:8090/default/token" -Body @{grant_type="client_credentials";client_id="demo";client_secret="demo";scope="transactions"}).access_token
```

## Seeded data — customer `P-0123456789`, 3 accounts, October 2020
| id (suffix) | account (IBAN) | currency | amount | type | value date |
|---|---|---|---|---|---|
| …4e01 | GB29-0000-0000-0000-0001 | GBP | 100.00 | CREDIT | 2020-10-01 |
| …4e02 | GB29-0000-0000-0000-0001 | GBP | -50.00 | DEBIT | 2020-10-02 |
| …5f03 | DE89-0000-0000-0000-0002 | EUR | 200.00 | CREDIT | 2020-10-03 |
| …6a05 | CH93-0000-0000-0000-0000-0 | CHF | 75.00 | CREDIT | 2020-10-04 |
| …5f04 | DE89-0000-0000-0000-0002 | EUR | -30.00 | DEBIT | 2020-10-05 |
| …6a06 | CH93-0000-0000-0000-0000-0 | CHF | -25.00 | DEBIT | 2020-10-06 |

---

## TC1 — full month, converted to CHF
**Request:** `GET /api/v1/transactions?yearMonth=2020-10&targetCurrency=CHF` (valid JWT)
**Expected:** `200`, `totalElements=6`, `totalPages=1`, ordered by value date:

| value date / ccy | source amount | → CHF (×1.10, or ×1.0 if CHF) |
|---|---|---|
| 2020-10-01 GBP | 100.00 | 110.00 |
| 2020-10-02 GBP | -50.00 | -55.00 |
| 2020-10-03 EUR | 200.00 | 220.00 |
| 2020-10-04 CHF | 75.00 | **75.00** (same-currency = rate 1.0) |
| 2020-10-05 EUR | -30.00 | -33.00 |
| 2020-10-06 CHF | -25.00 | **-25.00** (same-currency) |

`pageTotals`: **totalCredit = 405.00**, **totalDebit = -113.00**, currency CHF.

## TC2 — pagination (`size=2`)
`...&targetCurrency=CHF&page=N&size=2` → `totalPages=3`:

| page | transactions (→CHF) | totalCredit | totalDebit |
|---|---|---|---|
| 0 | 110.00, -55.00 | 110.00 | -55.00 |
| 1 | 220.00, 75.00 | 295.00 | 0.00 |
| 2 | -33.00, -25.00 | 0.00 | -58.00 |

(Totals are **per page**, exactly as the brief requires.)

## TC3 — empty month
**Request:** `...?yearMonth=2020-09&targetCurrency=CHF` → `200`, `totalElements=0`, empty `transactions`, totals `0.00`.

## TC4 — authentication / authorization
| Case | Expected |
|---|---|
| No `Authorization` header | `401` |
| Malformed/expired bearer token | `401` |
| Valid token | `200` — results scoped to the `sub` claim's owned IBANs only; the customer is never a request parameter (no IDOR/BOLA) |

## TC5 — invalid input
| Request | Expected |
|---|---|
| `targetCurrency=ZZZ` (not ISO 4217) | `400` |
| missing `yearMonth` | `400` |

---

## Live rates (`real-fx` profile)
```powershell
$env:APP_PROFILE="real-fx"; docker compose up -d --force-recreate app   # then re-seed
```
Same requests/structure as above, but converted amounts use the **current** ECB rate from
Frankfurter, so values vary by day. Rule: `amountInTargetCurrency = amount × currentRate`,
rounded HALF_EVEN to the target currency's fraction digits; CHF→CHF stays 1.0. Example seen:
GBP→CHF ≈ 1.06 → 100.00 GBP shows as 106.00 CHF.
