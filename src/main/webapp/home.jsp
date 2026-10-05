<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Online Fruit Shopping</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<header class="navbar">

    <div class="logo">
        FreshFruit
    </div>

    <div class="search-box">
        <input type="text" placeholder="Search fresh fruits...">
        <button>Search</button>
    </div>

    <div class="nav-actions">

        <%
            com.mycompany.onlinefruitshopping.model.User currentUser =
                    (com.mycompany.onlinefruitshopping.model.User)
                    session.getAttribute("user");

            String dashboardUrl = null;
            String dashboardName = null;

            if (currentUser != null && currentUser.getRoleName() != null) {

                String role = currentUser.getRoleName()
                        .trim()
                        .toUpperCase();

                switch (role) {

                    case "ADMIN":
                        dashboardUrl =
                                request.getContextPath() + "/admin/dashboard";
                        dashboardName = "Admin Dashboard";
                        break;

                    case "SHOPOWNER":
                    case "SHOP_OWNER":
                        dashboardUrl =
                                request.getContextPath() + "/store-owner/dashboard";
                        dashboardName = "Shop Owner Dashboard";
                        break;

                    case "DELIVERYSTAFF":
                        dashboardUrl =
                                request.getContextPath() + "/delivery/dashboard";
                        dashboardName = "Delivery Dashboard";
                        break;

                    case "CUSTOMER":
                        dashboardUrl =
                                request.getContextPath() + "/customer/dashboard";
                        dashboardName = "Customer Dashboard";
                        break;
                }
            }
        %>


        <!-- Cart: chỉ hiển thị icon giỏ hàng khi khách login -->
        <% if (currentUser != null) { %>
            <a href="#" onclick="alert('Giỏ hàng hiện tại đang trống.'); return false;" title="Giỏ hàng" style="display: inline-flex; align-items: center; gap: 6px; cursor: pointer;">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="20" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
                Giỏ hàng
            </a>

            <!-- My Account -->
            <a href="${pageContext.request.contextPath}/profile">
                My Account
            </a>
        <% } else { %>
            <a href="${pageContext.request.contextPath}/login">
                Đăng nhập
            </a>
        <% } %>


        <!-- Dashboard -->
        <% if (dashboardUrl != null) { %>

            <a href="<%= dashboardUrl %>">
                <%= dashboardName %>
            </a>

        <% } %>


        <!-- Logout -->
        <% if (currentUser != null) { %>

            <a class="logout"
               href="${pageContext.request.contextPath}/logout">
                Logout
            </a>

        <% } %>

    </div>

</header>


<nav class="category-bar">

    <a href="#">All Fruits</a>
    <a href="#">Tropical Fruits</a>
    <a href="#">Citrus Fruits</a>
    <a href="#">Berries</a>
    <a href="#">Seasonal Fruits</a>

</nav>


<section class="hero">

    <div class="hero-content">

        <p class="hero-small">
            FRESH • HEALTHY • NATURAL
        </p>

        <h1>
            Fresh Fruits<br>
            Delivered To Your Door
        </h1>

        <p>
            Enjoy fresh and delicious fruits every day.
            Quality products from trusted suppliers.
        </p>

        <button class="shop-button">
            Shop Now
        </button>

    </div>

</section>


<section class="section">

    <h2>Popular Categories</h2>

    <div class="category-grid">

        <div class="category-card">
            🍎
            <h3>Apples</h3>
        </div>

        <div class="category-card">
            🍊
            <h3>Oranges</h3>
        </div>

        <div class="category-card">
            🍌
            <h3>Bananas</h3>
        </div>

        <div class="category-card">
            🍉
            <h3>Watermelon</h3>
        </div>

        <div class="category-card">
            🍇
            <h3>Grapes</h3>
        </div>

        <div class="category-card">
            🥭
            <h3>Mango</h3>
        </div>

    </div>

</section>


<section class="section">

    <div class="section-header">

        <h2>Featured Fruits</h2>

        <a href="#">
            View All →
        </a>

    </div>


    <div class="product-grid">


        <!-- Product 1 -->
        <div class="product-card">

            <div class="product-image">
                🍎
            </div>

            <h3>Fresh Red Apple</h3>

            <p class="product-desc">
                Sweet and crispy apples
            </p>

            <div class="product-bottom">

                <span class="price">
                    $3.99 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 2 -->
        <div class="product-card">

            <div class="product-image">
                🍊
            </div>

            <h3>Fresh Orange</h3>

            <p class="product-desc">
                Juicy and rich in vitamin C
            </p>

            <div class="product-bottom">

                <span class="price">
                    $2.99 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 3 -->
        <div class="product-card">

            <div class="product-image">
                🍇
            </div>

            <h3>Premium Grapes</h3>

            <p class="product-desc">
                Fresh seedless grapes
            </p>

            <div class="product-bottom">

                <span class="price">
                    $5.49 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


        <!-- Product 4 -->
        <div class="product-card">

            <div class="product-image">
                🥭
            </div>

            <h3>Fresh Mango</h3>

            <p class="product-desc">
                Sweet tropical mango
            </p>

            <div class="product-bottom">

                <span class="price">
                    $4.49 / kg
                </span>

                <button>
                    Add +
                </button>

            </div>

        </div>


    </div>

</section>


<footer class="footer">

    <h3>🍊 FreshFruit</h3>

    <p>
        Fresh fruits. Healthy life.
    </p>

    <p>
        © 2026 Online Fruit Shopping
    </p>

</footer>


</body>
</html>