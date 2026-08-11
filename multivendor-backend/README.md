# Multi-Vendor E-Commerce — Backend (Spring Boot)

MVP backend for a multi-vendor marketplace. Covers **Authentication, Vendor, Product,
Cart, and Order (Cash on Delivery)** modules end-to-end, plus the Admin actions needed
to support them (vendor approval, category management, order monitoring).

> This is stage 1 of the project (backend API only). Payment gateway integration
> (Razorpay/UPI/cards), notifications (email/SMS/push), and the React frontend are
> intentionally left for later stages — see "What's not built yet" below.

## Tech stack

- Java 17, Spring Boot 3.3.2
- Spring Web, Spring Data JPA, Spring Security
- MySQL 8
- JWT (jjwt 0.12.6) for stateless auth
- Lombok, Bean Validation

## Project structure

```
src/main/java/com/multivendor/ecommerce/
├── config/          # SecurityConfig (CORS, JWT filter chain, route rules)
├── security/         # JwtUtil, JwtAuthFilter, CustomUserDetails(Service), entry point
├── entity/            # JPA entities (User, Vendor, Category, Product, Address, Cart, CartItem, Order, OrderItem)
│   └── enums/         # Role, OrderStatus, PaymentMethod, PaymentStatus
├── repository/        # Spring Data JPA repositories
├── service/           # Interfaces
│   └── impl/          # Implementations (business logic lives here)
├── controller/         # REST controllers
├── dto/
│   ├── request/        # Request payloads (validated)
│   └── response/       # Response payloads
├── exception/           # Custom exceptions + @RestControllerAdvice global handler
└── util/                 # ApiResponse<T> wrapper, SecurityUtils (current-user helper)
```

## Getting started

### 1. Prerequisites
- JDK 17+
- Maven 3.9+
- MySQL 8 running locally (or update `application.yml` to point elsewhere)

### 2. Create the database
The app auto-creates the schema (`ddl-auto: update`) and the database itself
(`createDatabaseIfNotExist=true`), so you only need MySQL running with valid credentials.

### 3. Configure credentials
Either edit `src/main/resources/application.yml`, or set environment variables:

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=some-long-random-secret-at-least-32-bytes
```

### 4. Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

### 5. Create an admin account
Admins can't self-register through `/api/auth/register` (that endpoint only issues
CUSTOMER/VENDOR accounts). After the app has started once (so the `users` table
exists), run the seed script:

```
src/main/resources/sql/seed_admin.sql
```

It inserts `[email protected]` / `Admin@123` (BCrypt-hashed). **Change this password
before using it anywhere near production.**

## Auth model

- JWT is returned from `/api/auth/register` and `/api/auth/login`.
- Send it as `Authorization: Bearer <token>` on subsequent requests.
- Roles: `CUSTOMER`, `VENDOR`, `ADMIN`. A user becomes a vendor by registering a store
  via `POST /api/vendor/store` after signing up with role `VENDOR` — the store then
  needs admin approval before the vendor can list products.

## API reference

All responses are wrapped as:
```json
{ "success": true, "message": "...", "data": { ... } }
```

### Auth (public)
| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register as CUSTOMER or VENDOR |
| POST | `/api/auth/login` | Login, returns JWT |

### Products & Categories (public read)
| Method | Path | Description |
|---|---|---|
| GET | `/api/products?keyword=&categoryId=&minPrice=&maxPrice=&page=&size=&sortBy=&direction=` | Browse/search/filter products |
| GET | `/api/products/{id}` | Product detail |
| GET | `/api/products/{id}/reviews` | Reviews for a product (public) |
| POST | `/api/products/{id}/reviews` | Submit/update my review `{ rating: 1-5, comment }` (auth) |
| GET | `/api/products/{id}/reviews/mine` | My review for this product (auth) |
| DELETE | `/api/products/{id}/reviews/mine` | Delete my review (auth) |
| GET | `/api/categories?topLevelOnly=false` | List categories (raw, admin-shaped) |
| GET | `/api/categories/storefront` | Every category with active product count (self + subcategories) — powers the storefront nav/tiles |
| GET | `/api/categories/{id}` | Category detail |
| GET | `/api/categories/{id}/breadcrumb` | Root-to-leaf path, e.g. Electronics → Phones → Smartphones |

### Wishlist (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/wishlist` | My wishlist |
| POST | `/api/wishlist/{productId}` | Add (idempotent) |
| DELETE | `/api/wishlist/{productId}` | Remove |

### Cart (authenticated — any role)
| Method | Path | Description |
|---|---|---|
| GET | `/api/cart` | View cart |
| POST | `/api/cart/items` | Add item `{ productId, quantity }` |
| PATCH | `/api/cart/items/{cartItemId}` | Update quantity `{ quantity }` |
| DELETE | `/api/cart/items/{cartItemId}` | Remove item |
| DELETE | `/api/cart` | Clear cart |

### Addresses (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/addresses` | List my addresses |
| POST | `/api/addresses` | Add address |
| PUT | `/api/addresses/{id}` | Update address |
| DELETE | `/api/addresses/{id}` | Delete address |

### Orders (authenticated)
| Method | Path | Description |
|---|---|---|
| POST | `/api/orders` | Place order from cart `{ addressId, paymentMethod: "COD" \| "CCAVENUE" }` — fans out into per-vendor sub-orders |
| GET | `/api/orders` | Order history (each order includes nested `subOrders`) |
| GET | `/api/orders/{id}` | Order detail |
| POST | `/api/orders/{id}/cancel` | Cancel every sub-order (only while all are still PLACED/CONFIRMED) |

### Payments (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/payments/my` | My payment history |
| GET | `/api/payments/order/{orderId}` | Payment for a specific order |
| POST | `/api/payments/ccavenue/initiate/{orderId}` | Get CCAvenue redirect fields |
| POST | `/api/payments/ccavenue/callback` | **Public** — CCAvenue calls this directly |

### Vendor (role: VENDOR)
| Method | Path | Description |
|---|---|---|
| POST | `/api/vendor/store` | Register store (pending approval) |
| GET | `/api/vendor/store` | My store profile — includes commission breakdown, rating, bank details |
| PUT | `/api/vendor/store` | Update store profile + bank details |
| POST | `/api/vendor/products` | Add product (requires approved store) |
| GET | `/api/vendor/products` | My products |
| PUT | `/api/vendor/products/{id}` | Update product |
| DELETE | `/api/vendor/products/{id}` | Soft-delete (deactivate) product |
| PATCH | `/api/vendor/products/{id}/stock` | Update stock `{ stock }` |
| GET | `/api/vendor/orders` | My sub-orders (each with items, status, commission, shipment) |
| PATCH | `/api/vendor/orders/{vendorOrderId}/status` | Update sub-order status `{ status }` |
| POST | `/api/vendor/orders/{vendorOrderId}/shipment` | Book a courier via Shiprocket |
| GET | `/api/vendor/orders/{vendorOrderId}/shipment` | View shipment/tracking |
| POST | `/api/vendor/orders/{vendorOrderId}/shipment/refresh` | Pull latest tracking status |
| GET | `/api/vendor/settlements` | My settlement (payout) history |
| GET | `/api/vendor/settlements/eligible` | Sub-orders that would be included in my next settlement |

### Admin (role: ADMIN)
| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/vendors/pending` | Vendors awaiting approval |
| GET | `/api/admin/vendors/approved` | Approved vendors |
| POST | `/api/admin/vendors/{id}/approve` | Approve vendor |
| POST | `/api/admin/vendors/{id}/reject` | Reject (deletes pending application) |
| PUT | `/api/admin/vendors/{id}/commission` | Set a vendor-specific commission override `{ commissionRate }` |
| POST | `/api/admin/categories` | Create category `{ name, description, parentId }` |
| PUT | `/api/admin/categories/{id}` | Update category |
| DELETE | `/api/admin/categories/{id}` | Delete category |
| GET | `/api/admin/orders` | All orders (monitoring) |
| POST | `/api/admin/vendor-orders/{vendorOrderId}/shipment` | Book a shipment on a vendor's behalf (fallback) |
| GET | `/api/admin/settlements` | All settlements |
| GET | `/api/admin/vendors/{vendorId}/settlements/eligible` | Preview a vendor's next settlement |
| POST | `/api/admin/vendors/{vendorId}/settlements/generate` | Generate a settlement for a vendor |
| POST | `/api/admin/settlements/{id}/mark-paid` | Record a settlement as paid `{ paymentReference, notes }` |

## Typical flow to test end-to-end

1. `POST /api/auth/register` with `role: VENDOR` → get token.
2. `POST /api/vendor/store` (as vendor) → store created, `approved: false`.
3. Log in as admin (seeded) → `GET /api/admin/vendors/pending` → `POST /api/admin/vendors/{id}/approve`.
4. As admin: `POST /api/admin/categories` to create at least one category.
5. As vendor: `POST /api/vendor/products` (now allowed since store is approved).
6. Register/login as CUSTOMER → `GET /api/products` → `POST /api/cart/items` →
   `POST /api/addresses` → `POST /api/orders`.
7. As vendor: `GET /api/vendor/orders` → `PATCH .../status` to move it through
   CONFIRMED → SHIPPED → DELIVERED. Moving a COD order's items all to DELIVERED
   automatically marks its payment SUCCESS (COD collected on delivery).

## Multi-vendor order model (sub-orders)

A customer's single checkout (`Order`) fans out into one `VendorOrder` (a
"sub-order") per vendor whose products were in the cart. This is the real
shape of a multi-vendor marketplace:

- **Order** — one per checkout. Owns payment (the customer pays once for the
  whole cart) and the shared delivery address. Its `status` is a derived
  summary across all its sub-orders (see `OrderServiceImpl.recomputeOrderStatus`).
- **VendorOrder** — one per vendor per checkout. Owns fulfilment status
  (PLACED → CONFIRMED → SHIPPED → DELIVERED, or CANCELLED/RETURNED), its own
  `Shipment` (see below), and the commission/payout math for that vendor's
  slice of the sale. Vendors only ever see/manage their own `VendorOrder`s.
- **OrderItem** — a line item, always owned by exactly one `VendorOrder`.

Customer-facing `OrderResponse` nests `subOrders` but deliberately excludes
commission/payout figures. The vendor/admin-facing `VendorOrderResponse`
includes them.

## Vendor-wise shipping (Shiprocket)

Shipping is booked **per sub-order**, not per order — each vendor packs and
ships their own items independently, so each `VendorOrder` gets its own
`Shipment` (courier, AWB, tracking status). A vendor books their own shipment
via `POST /api/vendor/orders/{vendorOrderId}/shipment`; admins have the same
capability as a fallback. See `SHIPPING_SETUP.md` for full setup — short
version: set `SHIPROCKET_ENABLED=true` plus your account email/password and
pickup location nickname. **I could not test this against a live Shiprocket
account from this environment** — verify the request/response field names
against their current API docs before relying on it (flagged throughout
`ShiprocketClient`/`ShippingServiceImpl`).

## Commission & settlement

Every `VendorOrder` snapshots a `commissionRate`, `commissionAmount`, and
`payoutAmount` at order-placement time. The rate is:

1. The vendor's own override (`Vendor.commissionRate`) if an admin has set
   one, else the platform default (`app.commission.default-rate-percent`).
2. **Adjusted by the vendor's average product rating** — see the ratings
   section below. This is the whole rate calculation, in `CommissionServiceImpl`.

Once a sub-order is `DELIVERED` and its order is fully paid, it becomes
**settlement-eligible**. An admin generates a `Settlement` (a payout batch)
covering every eligible, not-yet-settled sub-order for a vendor:

```
GET  /api/admin/vendors/{vendorId}/settlements/eligible   # preview what would be included
POST /api/admin/vendors/{vendorId}/settlements/generate    # bundle them into a PENDING settlement
POST /api/admin/settlements/{id}/mark-paid                 # record that the payout was sent
```

**Generating or marking a settlement paid only updates our own records** — it
does not itself move money. Actually transferring funds to a vendor's bank
account (NEFT/IMPS/UPI payout, or a manual transfer) is a separate step
outside this codebase; `Settlement.paymentReference` is where you record the
resulting transaction/UTR number after doing that manually or via a payout API.

Vendors can see their own settlement history at `GET /api/vendor/settlements`
and preview what they're owed so far at `GET /api/vendor/settlements/eligible`.

## Ratings, reviews & the commission link

Customers rate/review products (`POST /api/products/{id}/reviews`, one review
per customer per product, upsertable). A review is tagged `verifiedPurchase`
if the reviewer has a `DELIVERED` order containing that product, but isn't
required to have purchased — an unreviewable new product could otherwise
never get its first review.

**A vendor's average rating (across their whole catalog) adjusts their
effective commission rate**, applied as a multiplier on top of their base
rate (config in `CommissionProperties` / `app.commission.*`):

| Average rating | Multiplier | Effect |
|---|---|---|
| ≥ 4.5 | 0.85× | Vendor keeps more — reward for consistently good service |
| ≥ 4.0 | 1.00× | Standard rate |
| ≥ 3.0 | 1.10× | Small surcharge |
| < 3.0 | 1.25× | Larger surcharge |
| No reviews yet, or fewer than `minimumReviewsForAdjustment` (default 3) | 1.00× | Benefit of the doubt for new vendors |

This multiplier is recalculated fresh on every order (it's not cached on the
vendor), so a vendor's rate can drift up or down over time as their rating
changes — but each *past* `VendorOrder` keeps the rate that was in effect
when it was placed. Check `GET /api/vendor/store` (or the admin vendor list)
to see a vendor's current `averageRating`, `ratingMultiplier`,
`baseCommissionRate`, and `effectiveCommissionRate` broken out.

## Wishlist

Straightforward save-for-later, independent of cart/stock:

```
GET    /api/wishlist              # my wishlist
POST   /api/wishlist/{productId}  # add (idempotent)
DELETE /api/wishlist/{productId}  # remove
```

## Category-wise browsing

The storefront is organized around categories, not just search:

- **`GET /api/products?categoryId=X`** matches products in category `X` **and every subcategory beneath it** —
  browsing a parent category (e.g. "Electronics") shows products filed under any of its children
  (e.g. "Phones", "Laptops") too, resolved via `CategoryService.getDescendantIds`.
- **`GET /api/categories/storefront`** returns every category with its active product count
  (self + subcategories), which the frontend uses for both the sidebar nav and the "Shop by
  category" tile grid.
- **`GET /api/categories/{id}/breadcrumb`** returns the root-to-leaf path for breadcrumb display.

On the frontend, `Home.jsx` now has a persistent category sidebar (`CategorySidebar.jsx`,
expandable subcategories) alongside the product grid, a breadcrumb when a category is selected,
and a tile-grid landing view (`CategoryTiles.jsx`) when browsing with no filters. Category
selection lives in the URL (`?category=5`) so links are shareable.

## Payment — CCAvenue (real gateway, hosted redirect)

Cash on Delivery works out of the box. Online payment goes through **CCAvenue**,
a real hosted-checkout gateway — see `CCAVENUE_SETUP.md` for full setup steps,
the request/response flow, and what to verify before going live. Short version:

1. Get a CCAvenue merchant account (a **Test** account is enough for development)
   and note your Merchant ID, Working Key, and Access Code from their dashboard.
2. Set `CCAVENUE_ENABLED=true` plus the credential env vars (see `CCAVENUE_SETUP.md`).
3. `POST /api/orders` with `paymentMethod: "CCAVENUE"` creates the order unpaid,
   then `POST /api/payments/ccavenue/initiate/{orderId}` returns the fields needed
   to redirect the browser to CCAvenue's hosted page.
4. CCAvenue POSTs the result back to `/api/payments/ccavenue/callback` (public,
   no JWT — CCAvenue calls it directly), which updates the order/payment and
   redirects the browser to the frontend.

**I could not run a live transaction against CCAvenue from this environment**
(no network access, no real credentials) — the encryption scheme and parameter
list follow CCAvenue's publicly documented integration kit, but confirm against
their current docs and a real Test-account transaction before relying on it.

## What's not built yet (by design, per current scope)

- **Shipping Module**: entities (`Shipment`) exist but the Shiprocket API client,
  tracking sync, and admin "create shipment" action aren't wired up yet.
- **Notification Module**: no email/SMS/push. Hook these into `OrderServiceImpl`
  (order placed/status changed), `PaymentServiceImpl` (payment success/failure),
  and `AdminServiceImpl` (vendor approved) when ready.
- **Review Module**: not implemented.
- **Customer profile / admin customer list**: not implemented yet.
- **CCAvenue Refund API**: cancelling a paid order marks the payment `REFUNDED`
  in our own database only — it does not call CCAvenue to actually return funds.
  That needs CCAvenue's separate Refund API (extra approval required) or a manual
  refund from their merchant dashboard; see `PaymentServiceImpl.refund()`.
- **OTP verification / refresh tokens**: login is password-only for now; JWT has no
  refresh-token endpoint yet (config for `refresh-expiration-ms` is already present).
- **React frontend**: being extended alongside the backend — check the frontend
  project's own README for what's wired up so far.
- **Product images upload, variants, brands, coupons, reports/dashboards**: not
  implemented; `Product` currently has a single `imageUrl` string field.

## Security notes before production

- Replace `app.jwt.secret` with a strong, secret-managed value (never commit it).
- Replace the seeded admin password immediately.
- `ddl-auto: update` is convenient for development; use migrations (Flyway/Liquibase)
  for production instead.
- CORS is currently wide open to `http://localhost:*` for local frontend dev — restrict
  this to your real frontend origin(s) before deploying.
