
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%!
    private String esc(Object value) {
        if (value == null) return "";

        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    String cp = request.getContextPath();

    List<Map<String, Object>> categories =
            (List<Map<String, Object>>) request.getAttribute("categories");

    List<Map<String, Object>> shops =
            (List<Map<String, Object>>) request.getAttribute("shops");

    String error = request.getAttribute("error") == null
            ? ""
            : request.getAttribute("error").toString();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Add Product - FreshFruit</title>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f7fa;
            color: #263238;
        }

        .navbar {
            min-height: 70px;
            padding: 16px 5%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: white;
            border-bottom: 1px solid #e5e7eb;
        }

        .logo {
            font-size: 25px;
            font-weight: 700;
            color: #2e7d32;
        }

        .navbar a {
            color: #2e7d32;
            text-decoration: none;
            font-weight: 600;
        }

        .container {
            max-width: 900px;
            width: 94%;
            margin: 35px auto;
        }

        .card {
            padding: 28px;
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,.04);
        }

        h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }

        .subtitle {
            margin: 0 0 25px;
            color: #777;
            font-size: 14px;
        }

        .error {
            margin-bottom: 20px;
            padding: 12px 15px;
            color: #991b1b;
            background: #fee2e2;
            border-radius: 7px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .field {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .full {
            grid-column: 1 / -1;
        }

        label {
            font-size: 13px;
            font-weight: 700;
            color: #374151;
        }

        input, select, textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #d1d5db;
            border-radius: 7px;
            font-family: inherit;
            font-size: 14px;
            background: white;
        }

        input:focus, select:focus, textarea:focus {
            outline: none;
            border-color: #2e7d32;
            box-shadow: 0 0 0 2px rgba(46,125,50,.1);
        }

        textarea {
            min-height: 110px;
            resize: vertical;
        }

        .hint {
            color: #777;
            font-size: 12px;
        }

        .actions {
            display: flex;
            gap: 12px;
            margin-top: 28px;
            flex-wrap: wrap;
        }

        .btn {
            min-height: 43px;
            padding: 0 20px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border: none;
            border-radius: 7px;
            font-weight: 700;
            text-decoration: none;
            cursor: pointer;
        }

        .primary {
            color: white;
            background: #2e7d32;
        }

        .primary:hover {
            background: #256628;
        }

        .secondary {
            color: #444;
            background: #f1f3f5;
        }

        @media (max-width: 650px) {
            .form-grid { grid-template-columns: 1fr; }
            .full { grid-column: auto; }
            .card { padding: 20px; }
        }
    </style>
</head>

<body>


<header class="navbar">
    <div class="logo">FreshFruit</div>

    <a href="<%=cp%>/admin/product-management">
        ← Back to Products
    </a>
</header>


<main class="container">
    <section class="card">

        <h1>Add Product</h1>
        <p class="subtitle">
            Enter the product details and variant information.
        </p>

        <% if (!error.isEmpty()) { %>
            <div class="error"><%=esc(error)%></div>
        <% } %>

        <form method="post"
              action="<%=cp%>/admin/product-add">

            <div class="form-grid">

                <div class="field full">
                    <label for="productName">Product Name *</label>
                    <input id="productName"
                           name="productName"
                           type="text"
                           maxlength="150"
                           required>
                </div>

                <div class="field">
                    <label for="shopId">Shop *</label>
                    <select id="shopId" name="shopId" required>
                        <option value="">-- Select shop --</option>
                        <% if (shops != null) {
                            for (Map<String, Object> shop : shops) { %>
                                <option value="<%=esc(shop.get("shop_id"))%>">
                                    <%=esc(shop.get("shop_name"))%>
                                </option>
                        <%  }
                        } %>
                    </select>
                    <span class="hint">
                        Only active shops are available.
                    </span>
                </div>

                <div class="field">
                    <label for="categoryId">Category *</label>
                    <select id="categoryId" name="categoryId" required>
                        <option value="">-- Select category --</option>
                        <% if (categories != null) {
                            for (Map<String, Object> category : categories) { %>
                                <option value="<%=esc(category.get("category_id"))%>">
                                    <%=esc(category.get("category_name"))%>
                                </option>
                        <%  }
                        } %>
                    </select>
                </div>

                <div class="field full">
                    <label for="description">Description</label>
                    <textarea id="description"
                              name="description"
                              placeholder="Describe the product..."></textarea>
                </div>

                <div class="field">
                    <label for="origin">Origin</label>
                    <input id="origin"
                           name="origin"
                           type="text"
                           maxlength="100"
                           placeholder="e.g. Vietnam">
                </div>

                <div class="field">
                    <label for="imageUrl">Image URL</label>
                    <input id="imageUrl"
                           name="imageUrl"
                           type="url"
                           maxlength="255"
                           placeholder="https://example.com/fruit.jpg">
                </div>

                <div class="field">
                    <label for="sku">SKU *</label>
                    <input id="sku"
                           name="sku"
                           type="text"
                           maxlength="50"
                           required
                           placeholder="e.g. APPLE-0.5KG">
                </div>

                <div class="field">
                    <label for="unit">Unit</label>
                    <input id="unit"
                           name="unit"
                           type="text"
                           value="kg"
                           readonly>
                    <span class="hint">Variants are measured in kilograms.</span>
                </div>

                <div class="field">
                    <label for="price">Price *</label>
                    <input id="price"
                           name="price"
                           type="number"
                           min="0.01"
                           step="0.01"
                           required>
                </div>

                <div class="field">
                    <label for="commissionRate">Commission (%)</label>
                    <input id="commissionRate"
                           name="commissionRate"
                           type="number"
                           min="0"
                           max="100"
                           step="0.01"
                           value="10"
                           placeholder="e.g. 10">
                    <span class="hint">
                        Percentage the platform keeps on each sale
                        (0 - 100). Defaults to 10 % when left empty.
                    </span>
                </div>

                <div class="field">
                    <label for="weightValue">Weight (kg) *</label>
                    <input id="weightValue"
                           name="weightValue"
                           type="number"
                           min="0.01"
                           step="0.01"
                           required
                           placeholder="0.5">
                    <span class="hint">
                        Weight of one package (e.g. 0.5 = half a kilo).
                    </span>
                </div>

                <div class="field full">
                    <label for="packagingType">Packaging Label</label>
                    <input id="packagingType"
                           name="packagingType"
                           type="text"
                           maxlength="50"
                           placeholder="e.g. Bag 500g">
                </div>

            </div>

            <div class="actions">
                <button type="submit" class="btn primary">
                    Save Product
                </button>

                <a class="btn secondary"
                   href="<%=cp%>/admin/product-management">
                    Cancel
                </a>
            </div>

        </form>

    </section>
</main>

</body>
</html>
