package com.mycompany.onlinefruitshopping.dao;

import com.mycompany.onlinefruitshopping.model.Product;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // =========================================================
    // GET SHOP ID FOR USER (auto-creates if missing)
    // =========================================================
    public int getOrCreateShopIdForUser(int userId, String userName) throws SQLException {
        String findSql = "SELECT shop_id FROM shops WHERE owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("shop_id");
                }
            }
        }

        // Create default shop for this store owner
        String createSql = "INSERT INTO shops (owner_id, shop_name, description, status) VALUES (?, ?, ?, 'active')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(createSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, "Cửa hàng của " + (userName != null ? userName : "Shop Owner"));
            ps.setString(3, "Cửa hàng trái cây tươi sạch phân phối trực tiếp.");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    // =========================================================
    // SEARCH & FILTER PRODUCTS
    // =========================================================
    public List<Product> getProducts(Integer shopId, String keyword, Integer categoryId, String status) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    p.product_id,
                    p.shop_id,
                    s.shop_name,
                    p.category_id,
                    c.category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        if (shopId != null && shopId > 0) {
            sql.append(" AND p.shop_id = ?");
            params.add(shopId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.product_name LIKE ? OR p.origin LIKE ? OR c.category_name LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
            sql.append(" AND p.status = ?");
            params.add(status.trim().toLowerCase());
        }

        sql.append(" ORDER BY p.product_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    public List<Product> getAllProducts() throws SQLException {
        return getProducts(null, null, null, null);
    }

    public List<Product> getProductsByShop(int shopId) throws SQLException {
        return getProducts(shopId, null, null, null);
    }

    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================
    public Product getProductById(int productId) throws SQLException {
        String sql = """
                SELECT 
                    p.product_id,
                    p.shop_id,
                    s.shop_name,
                    p.category_id,
                    c.category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.product_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    // =========================================================
    // ADD NEW PRODUCT (WITH VARIANT, IMAGE & INITIAL BATCH)
    // =========================================================
    public boolean addProduct(Product product) throws SQLException {
        String insProduct = """
                INSERT INTO products (shop_id, category_id, product_name, description, origin, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String insVariant = """
                INSERT INTO product_variants (product_id, sku, unit, packaging_type, price)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insImage = """
                INSERT INTO product_images (product_id, image_url, sort_order)
                VALUES (?, ?, 1)
                """;

        String insBatch = """
                INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status)
                VALUES (?, ?, ?, DATEADD(day, 30, GETDATE()), CAST(GETDATE() AS DATE), 'active')
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int productId;
            try (PreparedStatement psP = conn.prepareStatement(insProduct, Statement.RETURN_GENERATED_KEYS)) {
                psP.setInt(1, product.getShopId() > 0 ? product.getShopId() : 1);
                psP.setInt(2, product.getCategoryId());
                psP.setString(3, product.getProductName());
                psP.setString(4, product.getDescription());
                psP.setString(5, product.getOrigin());
                psP.setString(6, (product.getStatus() != null && !product.getStatus().isEmpty()) ? product.getStatus() : "published");

                int affected = psP.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet rsGen = psP.getGeneratedKeys()) {
                    if (rsGen.next()) {
                        productId = rsGen.getInt(1);
                        product.setProductId(productId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert primary variant
            int variantId;
            try (PreparedStatement psV = conn.prepareStatement(insVariant, Statement.RETURN_GENERATED_KEYS)) {
                String sku = product.getSku();
                if (sku == null || sku.isBlank()) {
                    sku = "SKU-" + productId + "-" + (System.currentTimeMillis() % 10000);
                }
                String unit = product.getUnit();
                if (unit == null || (!unit.equals("kg") && !unit.equals("box") && !unit.equals("piece"))) {
                    unit = "kg";
                }
                String packaging = product.getPackagingType();
                if (packaging == null || packaging.isBlank()) {
                    packaging = "Mặc định";
                }

                psV.setInt(1, productId);
                psV.setString(2, sku);
                psV.setString(3, unit);
                psV.setString(4, packaging);
                psV.setBigDecimal(5, BigDecimal.valueOf(product.getPrice()));

                psV.executeUpdate();
                try (ResultSet rsV = psV.getGeneratedKeys()) {
                    if (rsV.next()) {
                        variantId = rsV.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Insert image
            String img = product.getImageUrl();
            if (img != null && !img.isBlank()) {
                try (PreparedStatement psI = conn.prepareStatement(insImage)) {
                    psI.setInt(1, productId);
                    psI.setString(2, img.trim());
                    psI.executeUpdate();
                }
            }

            // Insert initial inventory stock
            if (product.getStock() > 0) {
                try (PreparedStatement psB = conn.prepareStatement(insBatch)) {
                    psB.setInt(1, variantId);
                    psB.setString(2, "BATCH-P" + productId);
                    psB.setBigDecimal(3, BigDecimal.valueOf(product.getStock()));
                    psB.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // =========================================================
    // EDIT PRODUCT INFORMATION
    // =========================================================
    public boolean updateProduct(Product product) throws SQLException {
        String updProduct = """
                UPDATE products
                SET category_id = ?, product_name = ?, description = ?, origin = ?, status = ?
                WHERE product_id = ?
                """;

        String updVariant = """
                UPDATE product_variants
                SET price = ?, unit = ?, packaging_type = ?
                WHERE product_id = ?
                """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psP = conn.prepareStatement(updProduct)) {
                psP.setInt(1, product.getCategoryId());
                psP.setString(2, product.getProductName());
                psP.setString(3, product.getDescription());
                psP.setString(4, product.getOrigin());
                psP.setString(5, product.getStatus());
                psP.setInt(6, product.getProductId());
                psP.executeUpdate();
            }

            // Update variant price / unit
            String unit = product.getUnit();
            if (unit == null || (!unit.equals("kg") && !unit.equals("box") && !unit.equals("piece"))) {
                unit = "kg";
            }
            String packaging = product.getPackagingType();
            if (packaging == null || packaging.isBlank()) {
                packaging = "Mặc định";
            }

            int variantUpdated = 0;
            try (PreparedStatement psV = conn.prepareStatement(updVariant)) {
                psV.setBigDecimal(1, BigDecimal.valueOf(product.getPrice()));
                psV.setString(2, unit);
                psV.setString(3, packaging);
                psV.setInt(4, product.getProductId());
                variantUpdated = psV.executeUpdate();
            }

            // If no variant existed yet, insert one
            if (variantUpdated == 0) {
                String insV = "INSERT INTO product_variants (product_id, sku, unit, packaging_type, price) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psInsV = conn.prepareStatement(insV)) {
                    psInsV.setInt(1, product.getProductId());
                    psInsV.setString(2, "SKU-" + product.getProductId() + "-" + (System.currentTimeMillis() % 10000));
                    psInsV.setString(3, unit);
                    psInsV.setString(4, packaging);
                    psInsV.setBigDecimal(5, BigDecimal.valueOf(product.getPrice()));
                    psInsV.executeUpdate();
                }
            }

            // Update Image
            String img = product.getImageUrl();
            if (img != null && !img.isBlank()) {
                String checkImg = "SELECT image_id FROM product_images WHERE product_id = ?";
                boolean hasImg = false;
                try (PreparedStatement psCheck = conn.prepareStatement(checkImg)) {
                    psCheck.setInt(1, product.getProductId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        hasImg = rs.next();
                    }
                }

                if (hasImg) {
                    String updImg = "UPDATE product_images SET image_url = ? WHERE product_id = ?";
                    try (PreparedStatement psUpdImg = conn.prepareStatement(updImg)) {
                        psUpdImg.setString(1, img.trim());
                        psUpdImg.setInt(2, product.getProductId());
                        psUpdImg.executeUpdate();
                    }
                } else {
                    String insImg = "INSERT INTO product_images (product_id, image_url, sort_order) VALUES (?, ?, 1)";
                    try (PreparedStatement psInsImg = conn.prepareStatement(insImg)) {
                        psInsImg.setInt(1, product.getProductId());
                        psInsImg.setString(2, img.trim());
                        psInsImg.executeUpdate();
                    }
                }
            }

            // Update stock if specified
            if (product.getStock() >= 0) {
                String getVarId = "SELECT TOP 1 variant_id FROM product_variants WHERE product_id = ? ORDER BY variant_id ASC";
                int varId = -1;
                try (PreparedStatement psVar = conn.prepareStatement(getVarId)) {
                    psVar.setInt(1, product.getProductId());
                    try (ResultSet rs = psVar.executeQuery()) {
                        if (rs.next()) varId = rs.getInt(1);
                    }
                }
                if (varId > 0) {
                    String updBatch = "UPDATE inventory_batches SET quantity = ? WHERE variant_id = ? AND status = 'active'";
                    int batchAff = 0;
                    try (PreparedStatement psBatch = conn.prepareStatement(updBatch)) {
                        psBatch.setBigDecimal(1, BigDecimal.valueOf(product.getStock()));
                        psBatch.setInt(2, varId);
                        batchAff = psBatch.executeUpdate();
                    }
                    if (batchAff == 0 && product.getStock() > 0) {
                        String insBatch = "INSERT INTO inventory_batches (variant_id, batch_code, quantity, expiry_date, received_date, status) VALUES (?, ?, ?, DATEADD(day, 30, GETDATE()), CAST(GETDATE() AS DATE), 'active')";
                        try (PreparedStatement psInsBatch = conn.prepareStatement(insBatch)) {
                            psInsBatch.setInt(1, varId);
                            psInsBatch.setString(2, "BATCH-P" + product.getProductId());
                            psInsBatch.setBigDecimal(3, BigDecimal.valueOf(product.getStock()));
                            psInsBatch.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================
    public boolean deleteProduct(int productId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete inventory batches & adjustments for variants of this product
            String delAdj = """
                    DELETE FROM inventory_adjustments
                    WHERE batch_id IN (
                        SELECT batch_id FROM inventory_batches
                        WHERE variant_id IN (SELECT variant_id FROM product_variants WHERE product_id = ?)
                    )
                    """;
            try (PreparedStatement ps = conn.prepareStatement(delAdj)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            String delBatches = """
                    DELETE FROM inventory_batches
                    WHERE variant_id IN (SELECT variant_id FROM product_variants WHERE product_id = ?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(delBatches)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            // 2. Delete product (variants and images cascade delete)
            String delProd = "DELETE FROM products WHERE product_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delProd)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    // =========================================================
    // TOGGLE STATUS (AVAILABILITY)
    // =========================================================
    public boolean toggleProductStatus(int productId, Integer shopId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                UPDATE products
                SET status = CASE WHEN status = 'published' THEN 'hidden' ELSE 'published' END
                WHERE product_id = ?
                """);
        if (shopId != null) {
            sql.append(" AND shop_id = ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, productId);
            if (shopId != null) {
                ps.setInt(2, shopId);
            }
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // CUSTOMER: MULTI-FIELD SEARCH & ADVANCED FILTER
    // =========================================================
    public List<Product> searchAndFilterProducts(String keyword, Integer categoryId, List<String> origins,
                                                Double minPrice, Double maxPrice, Boolean inStockOnly,
                                                String sortBy, int page, int pageSize) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT 
                    p.product_id,
                    p.shop_id,
                    ISNULL(s.shop_name, N'FreshFruit Shop') AS shop_name,
                    p.category_id,
                    ISNULL(c.category_name, N'Khác') AS category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY price ASC, variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.status = 'published'
                """);

        List<Object> params = new ArrayList<>();
        buildFilterClauses(sql, params, keyword, categoryId, origins, minPrice, maxPrice, inStockOnly);

        // Sorting
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY v.price ASC, p.product_id DESC");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY v.price DESC, p.product_id DESC");
        } else if ("name_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY p.product_name ASC");
        } else if ("stock_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY b.stock DESC, p.product_id DESC");
        } else {
            sql.append(" ORDER BY p.product_id DESC");
        }

        // Pagination
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 12;
        int offset = (page - 1) * pageSize;
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    public int countFilteredProducts(String keyword, Integer categoryId, List<String> origins,
                                    Double minPrice, Double maxPrice, Boolean inStockOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY price ASC, variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.status = 'published'
                """);

        List<Object> params = new ArrayList<>();
        buildFilterClauses(sql, params, keyword, categoryId, origins, minPrice, maxPrice, inStockOnly);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void buildFilterClauses(StringBuilder sql, List<Object> params,
                                    String keyword, Integer categoryId, List<String> origins,
                                    Double minPrice, Double maxPrice, Boolean inStockOnly)
            throws SQLException {
        // Multi-field search
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                     AND (
                        p.product_name LIKE ? 
                        OR p.origin LIKE ? 
                        OR c.category_name LIKE ? 
                        OR p.description LIKE ? 
                        OR s.shop_name LIKE ?
                        OR EXISTS (
                            SELECT 1 FROM product_variants pv 
                            WHERE pv.product_id = p.product_id 
                            AND (pv.packaging_type LIKE ? OR pv.sku LIKE ?)
                        )
                    )
                    """);
            String kw = "%" + keyword.trim() + "%";
            for (int i = 0; i < 7; i++) {
                params.add(kw);
            }
        }

        // Category. Picking a parent category also shows the products of
        // every category underneath it, so the parent / child tree really
        // means something to the customer.
        if (categoryId != null && categoryId > 0) {
            List<Integer> categoryIds = collectCategoryIdsWithDescendants(categoryId);
            sql.append(" AND p.category_id IN (");
            for (int i = 0; i < categoryIds.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
                params.add(categoryIds.get(i));
            }
            sql.append(")");
        }

        // Origins filter
        if (origins != null && !origins.isEmpty()) {
            sql.append(" AND p.origin IN (");
            for (int i = 0; i < origins.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
                params.add(origins.get(i).trim());
            }
            sql.append(")");
        }

        // Price range
        if (minPrice != null && minPrice >= 0) {
            sql.append(" AND v.price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null && maxPrice > 0) {
            sql.append(" AND v.price <= ?");
            params.add(maxPrice);
        }

        // In stock only
        if (Boolean.TRUE.equals(inStockOnly)) {
            sql.append(" AND ISNULL(b.stock, 0) > 0");
        }
    }

    /**
     * Returns the given category plus every category below it (children,
     * grand-children, ...). Used so that filtering the customer catalog by
     * a parent category also returns the products filed under its
     * sub-categories.
     *
     * <p>The category tree is tiny, so the walk is done in Java with a
     * visited guard against a (pre-existing) cycle in the data.</p>
     */
    private List<Integer> collectCategoryIdsWithDescendants(int rootCategoryId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        ids.add(rootCategoryId);

        String sql = "SELECT category_id FROM categories WHERE parent_id = ?";
        java.util.Set<Integer> visited = new java.util.HashSet<>();
        visited.add(rootCategoryId);

        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        queue.add(rootCategoryId);

        try (Connection conn = DBConnection.getConnection()) {
            while (!queue.isEmpty()) {
                int current = queue.poll();
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, current);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int childId = rs.getInt("category_id");
                            if (visited.add(childId)) {
                                ids.add(childId);
                                queue.add(childId);
                            }
                        }
                    }
                }
            }
        }
        return ids;
    }

    // =========================================================
    // DISTINCT ORIGINS
    // =========================================================
    public List<String> getDistinctOrigins() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT origin FROM products WHERE status = 'published' AND origin IS NOT NULL AND RTRIM(LTRIM(origin)) <> '' ORDER BY origin ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("origin"));
            }
        }
        return list;
    }

    // =========================================================
    // FEATURED PRODUCTS (HOMEPAGE)
    // =========================================================
    public List<Product> getFeaturedProducts(int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = """
                SELECT TOP (?)
                    p.product_id,
                    p.shop_id,
                    ISNULL(s.shop_name, N'FreshFruit Shop') AS shop_name,
                    p.category_id,
                    ISNULL(c.category_name, N'Khác') AS category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY price ASC, variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.status = 'published'
                ORDER BY p.product_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit <= 0 ? 8 : limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    // =========================================================
    // RELATED PRODUCTS (SAME CATEGORY)
    // =========================================================
    public List<Product> getRelatedProducts(int categoryId, int excludeProductId, int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = """
                SELECT TOP (?)
                    p.product_id,
                    p.shop_id,
                    ISNULL(s.shop_name, N'FreshFruit Shop') AS shop_name,
                    p.category_id,
                    ISNULL(c.category_name, N'Khác') AS category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY price ASC, variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.status = 'published'
                  AND p.product_id <> ?
                  AND (p.category_id = ? OR ? = 0)
                ORDER BY p.product_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit <= 0 ? 4 : limit);
            ps.setInt(2, excludeProductId);
            ps.setInt(3, categoryId);
            ps.setInt(4, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    // =========================================================
    // SEARCH SUGGESTIONS (AUTOCOMPLETE)
    // =========================================================
    public List<Product> getSearchSuggestions(String query, int limit) throws SQLException {
        List<Product> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return list;
        }

        String sql = """
                SELECT TOP (?)
                    p.product_id,
                    p.shop_id,
                    ISNULL(s.shop_name, N'FreshFruit Shop') AS shop_name,
                    p.category_id,
                    ISNULL(c.category_name, N'Khác') AS category_name,
                    p.product_name,
                    p.description,
                    p.origin,
                    p.status,
                    p.created_at,
                    ISNULL(v.price, 0) AS price,
                    ISNULL(v.unit, 'kg') AS unit,
                    ISNULL(v.packaging_type, N'Mặc định') AS packaging_type,
                    ISNULL(v.sku, '') AS sku,
                    ISNULL(img.image_url, '') AS image_url,
                    ISNULL(b.stock, 0) AS stock
                FROM products p
                LEFT JOIN shops s ON p.shop_id = s.shop_id
                LEFT JOIN categories c ON p.category_id = c.category_id
                OUTER APPLY (
                    SELECT TOP 1 variant_id, sku, unit, packaging_type, price
                    FROM product_variants
                    WHERE product_id = p.product_id
                    ORDER BY price ASC, variant_id ASC
                ) v
                OUTER APPLY (
                    SELECT TOP 1 image_url
                    FROM product_images
                    WHERE product_id = p.product_id
                    ORDER BY sort_order ASC, image_id ASC
                ) img
                OUTER APPLY (
                    SELECT ISNULL(SUM(quantity), 0) AS stock
                    FROM inventory_batches
                    WHERE variant_id = v.variant_id AND status = 'active'
                ) b
                WHERE p.status = 'published'
                  AND (
                    p.product_name LIKE ?
                    OR p.origin LIKE ?
                    OR c.category_name LIKE ?
                    OR EXISTS (
                        SELECT 1 FROM product_variants pv 
                        WHERE pv.product_id = p.product_id 
                        AND (pv.packaging_type LIKE ? OR pv.sku LIKE ?)
                    )
                  )
                ORDER BY p.product_name ASC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit <= 0 ? 5 : limit);
            String kw = "%" + query.trim() + "%";
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            ps.setString(5, kw);
            ps.setString(6, kw);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    // Helper mapper
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setShopId(rs.getInt("shop_id"));
        p.setShopName(rs.getString("shop_name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setOrigin(rs.getString("origin"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setPrice(rs.getDouble("price"));
        p.setUnit(rs.getString("unit"));
        p.setPackagingType(rs.getString("packaging_type"));
        p.setSku(rs.getString("sku"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStock(rs.getDouble("stock"));
        return p;
    }
}
