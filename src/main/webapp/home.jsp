<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>

<head>

    <meta charset="UTF-8">

    <title>Online Fruit Shopping</title>

    <style>

        /* =====================================================
           GLOBAL
           ===================================================== */

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        html {
            scroll-behavior: smooth;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #f8faf7;
            color: #333;
            line-height: 1.5;
        }

        a {
            text-decoration: none;
        }

        button,
        input {
            font-family: inherit;
        }


        /* =====================================================
           NAVBAR
           ===================================================== */

        .navbar {
            min-height: 75px;
            background: #ffffff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 6%;
            border-bottom: 1px solid #e8eee6;
            gap: 25px;
        }

        .logo {
            font-size: 25px;
            font-weight: bold;
            color: #3d8b40;
            white-space: nowrap;
        }

        .search-box {
            flex: 1;
            max-width: 520px;
            display: flex;
        }

        .search-box input {
            width: 100%;
            padding: 12px 16px;
            border: 1px solid #d9e2d7;
            border-right: none;
            border-radius: 8px 0 0 8px;
            outline: none;
            font-size: 14px;
        }

        .search-box input:focus {
            border-color: #4caf50;
        }

        .search-box button {
            border: none;
            background: #4caf50;
            color: white;
            padding: 0 20px;
            border-radius: 0 8px 8px 0;
            cursor: pointer;
            font-weight: bold;
        }

        .search-box button:hover {
            background: #388e3c;
        }

        .nav-actions {
            display: flex;
            align-items: center;
            gap: 18px;
        }

        .nav-actions a {
            color: #444;
            font-size: 14px;
            transition: 0.2s;
        }

        .nav-actions a:hover {
            color: #4caf50;
        }

        .nav-actions .logout {
            color: #e74c3c;
        }

        .nav-actions .logout:hover {
            color: #c0392b;
        }


        /* =====================================================
           CATEGORY BAR
           ===================================================== */

        .category-bar {
            background: #ffffff;
            padding: 14px 6%;
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 40px;
            border-bottom: 1px solid #edf1eb;
        }

        .category-bar a {
            color: #555;
            font-size: 15px;
            transition: 0.2s;
        }

        .category-bar a:hover {
            color: #4caf50;
        }


        /* =====================================================
           HERO
           ===================================================== */

        .hero {
            min-height: 420px;

            background:
                linear-gradient(
                    90deg,
                    rgba(40, 100, 42, 0.90),
                    rgba(76, 175, 80, 0.45)
                ),
                linear-gradient(
                    135deg,
                    #dff3d9,
                    #b7dfae
                );

            display: flex;
            align-items: center;
            padding: 60px 8%;
        }

        .hero-content {
            max-width: 650px;
            color: white;
        }

        .hero-small {
            font-size: 13px;
            letter-spacing: 2px;
            margin-bottom: 15px;
            font-weight: bold;
        }

        .hero h1 {
            font-size: 52px;
            line-height: 1.15;
            margin-bottom: 20px;
        }

        .hero-content > p:not(.hero-small) {
            font-size: 18px;
            line-height: 1.6;
            max-width: 580px;
        }

        .shop-button {
            margin-top: 28px;
            padding: 13px 28px;
            border: none;
            border-radius: 8px;
            background: white;
            color: #3d8b40;
            font-weight: bold;
            cursor: pointer;
            font-size: 15px;
            transition: 0.2s;
        }

        .shop-button:hover {
            background: #f1f1f1;
            transform: translateY(-2px);
        }


        /* =====================================================
           GENERAL SECTIONS
           ===================================================== */

        .section {
            width: 88%;
            max-width: 1200px;
            margin: 55px auto;
        }

        .section h2 {
            font-size: 30px;
            margin-bottom: 25px;
            color: #234d25;
        }

        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .section-header h2 {
            margin-bottom: 0;
        }

        .section-header a {
            color: #4caf50;
            font-weight: bold;
        }

        .section-header a:hover {
            color: #388e3c;
        }


        /* =====================================================
           CATEGORY CARDS
           ===================================================== */

        .category-grid {
            display: grid;
            grid-template-columns: repeat(6, 1fr);
            gap: 18px;
        }

        .category-card {
            background: white;
            border: 1px solid #edf1eb;
            border-radius: 12px;
            padding: 25px 15px;
            text-align: center;
            font-size: 38px;
            transition: 0.2s;
            cursor: pointer;
        }

        .category-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
        }

        .category-card h3 {
            font-size: 16px;
            margin-top: 12px;
            color: #333;
        }


        /* =====================================================
           PRODUCT CARDS
           ===================================================== */

        .product-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 22px;
        }

        .product-card {
            background: white;
            border: 1px solid #edf1eb;
            border-radius: 12px;
            padding: 20px;
            transition: 0.2s;
        }

        .product-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
        }

        .product-image {
            height: 170px;
            background: #f1f7ee;
            border-radius: 10px;

            display: flex;
            justify-content: center;
            align-items: center;

            font-size: 90px;
            margin-bottom: 18px;
        }

        .product-card h3 {
            margin-bottom: 8px;
            color: #333;
        }

        .product-desc {
            color: #777;
            font-size: 14px;
            margin-bottom: 18px;
        }

        .product-bottom {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 10px;
        }

        .price {
            color: #3d8b40;
            font-weight: bold;
            font-size: 15px;
        }

        .product-bottom button {
            border: none;
            background: #4caf50;
            color: white;
            padding: 8px 14px;
            border-radius: 6px;
            cursor: pointer;
            font-weight: bold;
        }

        .product-bottom button:hover {
            background: #388e3c;
        }


        /* =====================================================
           FOOTER
           ===================================================== */

        .footer {
            margin-top: 70px;
            background: #234d25;
            color: white;
            text-align: center;
            padding: 40px 20px;
        }

        .footer h3 {
            margin-bottom: 10px;
            font-size: 22px;
        }

        .footer p {
            margin: 8px 0;
            opacity: 0.8;
        }


        /* =====================================================
           RESPONSIVE
           ===================================================== */

        @media (max-width: 1000px) {

            .category-grid {
                grid-template-columns: repeat(3, 1fr);
            }

            .product-grid {
                grid-template-columns: repeat(2, 1fr);
            }

            .navbar {
                flex-wrap: wrap;
                padding: 18px 5%;
            }

            .search-box {
                order: 3;
                width: 100%;
                max-width: none;
            }

            .hero h1 {
                font-size: 42px;
            }
        }


        @media (max-width: 600px) {

            .navbar {
                gap: 15px;
            }

            .logo {
                font-size: 21px;
            }

            .nav-actions {
                width: 100%;
                justify-content: center;
                flex-wrap: wrap;
            }

            .category-bar {
                gap: 18px;
                flex-wrap: wrap;
                justify-content: center;
            }

            .hero {
                min-height: 360px;
                padding: 45px 7%;
            }

            .hero h1 {
                font-size: 34px;
            }

            .hero-content > p:not(.hero-small) {
                font-size: 16px;
            }

            .section {
                width: 92%;
                margin: 40px auto;
            }

            .section h2 {
                font-size: 25px;
            }

            .category-grid {
                grid-template-columns: repeat(2, 1fr);
            }

            .product-grid {
                grid-template-columns: 1fr;
            }
        }

    </style>

</head>


<body>


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
                dashboardUrl =
                        request.getContextPath() + "/shopowner/dashboard";
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


<!-- =====================================================
     NAVBAR
     ===================================================== -->

<header class="navbar">

    <div class="logo">
        🍊 FreshFruit
    </div>


    <div class="search-box">

        <input
            type="text"
            placeholder="Search fresh fruits...">

        <button>
            Search
        </button>

    </div>


    <div class="nav-actions">

        <a href="${pageContext.request.contextPath}/profile">
            My Account
        </a>


        <% if (dashboardUrl != null) { %>

            <a href="<%= dashboardUrl %>">
                <%= dashboardName %>
            </a>

        <% } %>


        <% if (currentUser != null) { %>

            <a
                class="logout"
                href="${pageContext.request.contextPath}/logout">
                Logout
            </a>

        <% } %>

    </div>

</header>


<!-- =====================================================
     CATEGORY BAR
     ===================================================== -->

<nav class="category-bar">

    <a href="#">All Fruits</a>

    <a href="#">Tropical Fruits</a>

    <a href="#">Citrus Fruits</a>

    <a href="#">Berries</a>

    <a href="#">Seasonal Fruits</a>

</nav>


<!-- =====================================================
     HERO
     ===================================================== -->

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


<!-- =====================================================
     POPULAR CATEGORIES
     ===================================================== -->

<section class="section">

    <h2>
        Popular Categories
    </h2>


    <div class="category-grid">


        <div class="category-card">

            🍎

            <h3>
                Apples
            </h3>

        </div>


        <div class="category-card">

            🍊

            <h3>
                Oranges
            </h3>

        </div>


        <div class="category-card">

            🍌

            <h3>
                Bananas
            </h3>

        </div>


        <div class="category-card">

            🍉

            <h3>
                Watermelon
            </h3>

        </div>


        <div class="category-card">

            🍇

            <h3>
                Grapes
            </h3>

        </div>


        <div class="category-card">

            🥭

            <h3>
                Mango
            </h3>

        </div>


    </div>

</section>


<!-- =====================================================
     FEATURED FRUITS
     ===================================================== -->

<section class="section">


    <div class="section-header">

        <h2>
            Featured Fruits
        </h2>

        <a href="#">
            View All →
        </a>

    </div>


    <div class="product-grid">


        <!-- PRODUCT 1 -->

        <div class="product-card">

            <div class="product-image">
                🍎
            </div>

            <h3>
                Fresh Red Apple
            </h3>

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


        <!-- PRODUCT 2 -->

        <div class="product-card">

            <div class="product-image">
                🍊
            </div>

            <h3>
                Fresh Orange
            </h3>

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


        <!-- PRODUCT 3 -->

        <div class="product-card">

            <div class="product-image">
                🍇
            </div>

            <h3>
                Premium Grapes
            </h3>

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


        <!-- PRODUCT 4 -->

        <div class="product-card">

            <div class="product-image">
                🥭
            </div>

            <h3>
                Fresh Mango
            </h3>

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


<!-- =====================================================
     FOOTER
     ===================================================== -->

<footer class="footer">

    <h3>
        🍊 FreshFruit
    </h3>

    <p>
        Fresh fruits. Healthy life.
    </p>

    <p>
        © 2026 Online Fruit Shopping
    </p>

</footer>


</body>

</html>