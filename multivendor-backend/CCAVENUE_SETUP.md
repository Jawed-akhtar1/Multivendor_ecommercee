# CCAvenue integration — setup & testing guide

This backend integrates CCAvenue as a real, hosted-checkout payment gateway.
**This was implemented from CCAvenue's publicly documented integration kit
without the ability to make a live network call to their servers in this
environment** — treat everything below as "should be correct per their
published spec" and verify it against a real Test transaction before trusting
it for anything real.

## How CCAvenue's flow works (background)

Unlike a typical REST API, CCAvenue uses a **hosted redirect** model:

1. Your server builds a plain `key=value&key=value...` string of order details.
2. Your server AES-128-CBC encrypts that string with your **Working Key**
   (key = MD5 digest of the Working Key; IV is a fixed, publicly-known 16-byte
   array — the same for every merchant, it's the Working Key that's secret).
3. Your server returns that `encRequest` plus your `accessCode` to the browser.
4. **The browser** (not your server) submits an HTML form POST of those two
   fields to CCAvenue's transaction URL. This is a real page navigation —
   it can't be done as a background fetch/XHR.
5. The customer completes payment on CCAvenue's own hosted page (this is
   where card/UPI/net-banking/wallet selection happens — you don't build any
   of that UI yourself).
6. CCAvenue's servers POST an encrypted `encResp` directly to **your**
   `redirect_url` — this must be a publicly reachable HTTPS URL in production,
   since CCAvenue's servers are calling it, not the customer's browser.
7. Your server decrypts `encResp`, checks `order_status`, updates your
   records, and redirects the customer's browser to your frontend.

## What's implemented here

| Piece | File |
|---|---|
| AES-128-CBC encrypt/decrypt | `util/CCAvenueCrypto.java` |
| Config (merchant id, working key, access code, URLs) | `config/CCAvenueProperties.java` |
| Build request params / parse response params, order lookup, status update | `service/impl/PaymentServiceImpl.java` |
| `POST /api/payments/ccavenue/initiate/{orderId}` (auth required) | `controller/PaymentController.java` |
| `POST /api/payments/ccavenue/callback` (public — CCAvenue calls this directly) | `controller/PaymentController.java` |

## Setup steps

1. **Get a CCAvenue account.** Sign up at https://www.ccavenue.com — a **Test**
   merchant account is enough for development; you don't need to be live/KYC'd
   to start integrating.
2. **Find your credentials** in the CCAvenue merchant dashboard under
   Settings → API Keys: Merchant ID, Working Key, Access Code.
3. **Set environment variables** (or edit `application.yml` directly):
   ```bash
   export CCAVENUE_ENABLED=true
   export CCAVENUE_MERCHANT_ID=your_merchant_id
   export CCAVENUE_WORKING_KEY=your_working_key
   export CCAVENUE_ACCESS_CODE=your_access_code
   # Confirm this URL in your dashboard — test vs live URLs differ:
   export CCAVENUE_TRANSACTION_URL=https://securegw-stage.ccavenue.com/transaction/transaction.do?command=initiateTransaction
   # Must be a public HTTPS URL in production (e.g. via ngrok while developing locally):
   export CCAVENUE_REDIRECT_URL=https://your-public-host/api/payments/ccavenue/callback
   export CCAVENUE_FRONTEND_RETURN_URL=http://localhost:5173/orders
   ```
4. **Register the redirect URL** with CCAvenue if their dashboard requires
   whitelisting it (check current requirements — this has varied over time).
5. **Local testing note:** CCAvenue's servers must be able to reach your
   `CCAVENUE_REDIRECT_URL` over the public internet. `localhost` will not
   work for the callback step — use a tunnel (ngrok, Cloudflare Tunnel, etc.)
   while developing, and put the public tunnel URL in `CCAVENUE_REDIRECT_URL`.

## Testing checklist (do this before trusting the integration)

- [ ] Place an order with `paymentMethod: "CCAVENUE"`, confirm it's created
      with `paymentStatus: PENDING` and `status: PLACED`.
- [ ] Call `POST /api/payments/ccavenue/initiate/{orderId}`, confirm you get
      back `encRequest`, `accessCode`, `transactionUrl`.
- [ ] Auto-submit those as a form POST from the browser (see frontend's
      `Checkout.jsx`) and confirm you land on CCAvenue's actual Test hosted
      page — if you get an error page immediately, the encrypted request or
      access code is likely malformed; double check the Working Key.
- [ ] Complete a Test payment using CCAvenue's documented test card/UPI
      details (available in their Test account dashboard/docs).
- [ ] Confirm CCAvenue redirects back to `CCAVENUE_FRONTEND_RETURN_URL` and
      that `GET /api/orders/{id}` now shows `paymentStatus: PAID` and the
      payment's `transactionId` populated.
- [ ] Deliberately fail/abort a Test payment and confirm the order ends up
      with `paymentStatus: FAILED` rather than stuck `PENDING`.
- [ ] Check `PaymentServiceImpl.handleCCAvenueCallback` logs if anything
      looks wrong — it logs a warning on amount mismatches specifically.

## Known limitations / things to revisit

- **Refunds are local-only.** Cancelling a paid order marks the `Payment` row
  `REFUNDED` in our database but does not call CCAvenue to move money back —
  see `PaymentServiceImpl.refund()`. Real refunds need CCAvenue's separate
  Refund API (additional approval from CCAvenue) or manual processing from
  their merchant dashboard.
- **Stock is reserved optimistically.** Placing a CCAVENUE order decrements
  stock immediately, before payment is confirmed. If a customer abandons the
  CCAvenue page, that stock stays reserved with no automatic release. A
  production system would want a stock-hold expiry (e.g. a scheduled job that
  cancels stale unpaid orders after N minutes) — not implemented here.
- **Single billing/shipping address.** CCAvenue's request supports separate
  billing and delivery details; this integration sends the same address for
  both since the app only collects one address at checkout.
- **Verify the transaction/encryption details against CCAvenue's current kit.**
  Payment gateway APIs do change. If `initiate` produces an `encRequest` that
  CCAvenue's page rejects, start by downloading their latest Java/PHP/.NET
  integration kit from the merchant dashboard and diff the parameter list and
  crypto routine against `CCAvenueCrypto.java` / `PaymentServiceImpl`.
