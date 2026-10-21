# Demo guide - TienPQ features

Quick script for demoing the parts owned by **TienPQ**:

| Feature | What it is |
|---------|------------|
| 1.0 / 2.0 | Authentication, Profile, Address, User Admin |
| 16.0 | Role Management |
| 17.0 | Product Administration |
| 18.0 | Shop Owner Management + Commission reports |
| 19.0 | Order & Delivery Administration |
| 24.0 | Inventory & Pricing |

> The "Approve / Reject Shop Owner" task was dropped on purpose (see the project tracking).

---

## 1. Start

* App: **http://localhost:9999/OnlineFruitShopping**
* Password for every demo account: **`Tien123456`**

| Role | Email |
|------|-------|
| Admin | `he180293phamquangtien@gmail.com` |
| Shop Owner | `shopowner@freshfruit.com` |
| Customer | `customer@freshfruit.com` |
| Delivery Staff | `delivery@freshfruit.com` |
| Delivery Staff 2 | `shipper2@freshfruit.com` |
| Delivery Staff 3 | `shipper3@freshfruit.com` |

> Always pick the matching **Role** in the login drop-down.

## 2. Load the demo data (once)

```
sqlcmd -S localhost,1433 -U sa -P 123 -d OnlineFruitShopping -i database\demo_data.sql
```

`database\demo_data.sql` is **idempotent** (it refuses to run twice) and has a
**cleanup section** at the bottom to remove everything it added.

After loading, the database holds:

* 6 users - 1 admin, 1 owner, 1 customer, 3 delivery staff
* 4 categories, 4 products (one `published`, one `draft`, one `hidden`) and their variants
* 11 orders covering **every** status (pending_confirmation / preparing / shipping / delivered / cancelled / failed)
* 4 deliveries covering assigned / delivering / delivered / failed
* 8 inventory batches, one of them **low on stock** (ORANGE-2KG = 3)

## 3. Suggested demo order (about 7 minutes)

1. **Auth** - open `/register`, `/forgot-password`; show the validation errors.
2. **Admin dashboard** - log in as Admin, land on `/admin/dashboard`.
3. **17.0 Product** - `/admin/product-management`: search, filter status. Open
   `/admin/product-edit?id=1`, change the commission. Show a bad id
   (`?id=abc`) bouncing back to the list.
4. **18.0 Users** - `/admin/user-management`: search, filter by role, open a
   user detail. Try to **block the Admin account** (refused). Show
   `/admin/commission-report`.
5. **19.0 Orders** ⭐ - `/admin/orders` -> open an order detail.
   `/admin/delivery-assign`: assign order #4 to a shipper, then reassign /
   unassign. Show that assigning to a non-shipper is refused and that a note
   longer than 255 characters is refused.
6. **19.0 Issues** - `/admin/order-issues`: reopen a `failed` order or cancel it.
7. **19.0 Staff** - `/admin/delivery-staff`: show the workload and block /
   unblock a shipper.
8. **24.0 Inventory** - log in as the Shop Owner: `/store-owner/inventory`
   (stock), `/low-stock` (the low ORANGE batch), `/restock`, `/history`,
   `/pricing`. Show that a price of `0` or a negative price is refused.
9. **1.0 / 2.0 Profile** - log in as the Customer: `/profile`, `/addresses`.

## 4. Re-verify everything

```
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify3.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify_tienpq.ps1
```

* `tools\verify3.ps1` - 40 checks (shared UI + reports + inventory).
* `tools\verify_tienpq.ps1` - 59 checks covering every TienPQ feature, including
  the validation rules (bad input is rejected **and** never reaches the database).
