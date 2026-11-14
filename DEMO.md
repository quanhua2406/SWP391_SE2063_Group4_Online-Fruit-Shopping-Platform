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
   user detail / edit. Try to **block the Admin account** (refused). Show
   `/admin/roles` (role management). Show commission reports:
   `/admin/commission-report` (admin) and `/store-owner/commission-report`
   (shop owner).
5. **18.0 Shops** ⭐ - `/admin/shops`: the shop list (with owner, product count,
   status), shop dashboard, search by keyword / filter by status. Open a shop
   profile (`?action=detail&shopId=1`). Enter a **block reason** (required, max
   255 chars) and confirm **Khóa cửa hàng**: the owner account is locked, the
   shop becomes `suspended`, and its published products are hidden. Press
   **Mở khóa cửa hàng** to restore: the owner/shop are reactivated and only the
   products hidden by that block are republished (products the shop hid itself
   stay hidden). From the shop profile you can also open/edit the owner's
   account via user management.
6. **19.0 Orders** ⭐ - `/admin/orders` -> open an order detail
   (`/admin/order-detail?orderId=...`). `/admin/deliveries` monitors delivery
   status. `/admin/delivery-assign`: assign an eligible order to a delivery
   staff member, then reassign / unassign. Show that assigning to a non-shipper
   is refused and that a note longer than 255 characters is refused.
7. **19.0 Issues** - `/admin/order-issues`: reopen a `failed` order or cancel it.
8. **19.0 Staff** - `/admin/delivery-staff`: show the workload and block /
   unblock a shipper.
9. **24.0 Inventory** - log in as the Shop Owner: `/store-owner/inventory`
   (stock), `/low-stock` (the low ORANGE batch), `/restock`, `/history`,
   `/pricing`. Show that a price of `0` or a negative price is refused.
10. **1.0 / 2.0 Profile** - log in as the Customer: `/profile`, `/addresses`.

## 4. Re-verify everything

```
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify3.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools\verify_tienpq.ps1
```

* `tools\verify3.ps1` - 49 checks (shared UI + reports + inventory + shops).
* `tools\verify_tienpq.ps1` - 78 checks covering every TienPQ feature, including
  the validation rules (bad input is rejected **and** never reaches the database).
