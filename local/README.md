# Local end-to-end stack

Runs the API with all its dependencies so you can exercise the **real, secured**
endpoint with a genuine signed JWT.

| Service | URL | Purpose |
|---|---|---|
| app | http://localhost:8080 | the Transaction API |
| mock-oauth2 | http://localhost:8090 | issues real JWTs + serves JWKS |
| fx (WireMock) | http://localhost:8081 | stub FX provider (`/rate` → 1.10) |
| kafka | localhost:9092 | broker |

> If `localhost:8080` returns `401` for a valid token, make sure no
> `kubectl port-forward ... 8080` is running — it shadows the compose app.

## 1. Start everything
```bash
docker compose up -d --build
curl http://localhost:8080/actuator/health    # wait for {"status":"UP"}
```

## 2. Seed sample transactions (customer P-0123456789 owns CH93-0000-0000-0000-0000-0)
```bash
docker cp local/sample-transactions.jsonl kafka:/tmp/seed.jsonl
docker compose exec -T kafka sh -c \
  '/opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic transactions < /tmp/seed.jsonl'
```

## 3. Get a real JWT and call the API
**bash:**
```bash
TOKEN=$(curl -s -X POST http://localhost:8090/default/token \
  -d "grant_type=client_credentials&client_id=demo&client_secret=demo&scope=transactions" | jq -r .access_token)
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/transactions?yearMonth=2020-10&targetCurrency=CHF" | jq
```
**PowerShell:**
```powershell
$token = (Invoke-RestMethod -Method Post "http://localhost:8090/default/token" -Body @{
  grant_type="client_credentials"; client_id="demo"; client_secret="demo"; scope="transactions" }).access_token
Invoke-RestMethod "http://localhost:8080/api/v1/transactions?yearMonth=2020-10&targetCurrency=CHF" `
  -Headers @{ Authorization = "Bearer $token" } | ConvertTo-Json -Depth 6
```
Expect `200` with two transactions and `pageTotals` `110.00` credit / `-55.00` debit (FX stub rate 1.10). Without the token: `401`.

## 4. Swagger
http://localhost:8080/swagger-ui.html — **Authorize**, paste the token, "Try it out".

## 5. (Optional) live FX rates
By default the FX provider is the WireMock stub (fixed rate 1.10). To use **live ECB
rates** from [Frankfurter](https://frankfurter.dev), start the app with the `real-fx`
profile via the `APP_PROFILE` variable — **no rebuild needed** (the profile is a runtime
setting, not baked into the image):
```powershell
# PowerShell
$env:APP_PROFILE="real-fx"; docker compose up -d app
```
```bash
# bash
APP_PROFILE=real-fx docker compose up -d app
```
Back to the stub:
```bash
docker compose up -d app        # APP_PROFILE unset → default profile
```
Totals then use the current rate (e.g. GBP→CHF 1.0614 → 106.14 / -53.07). Tests and CI
always use the stub, so they stay deterministic.

## Tear down
```bash
docker compose down -v
```
