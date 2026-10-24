package com.mycompany.onlinefruitshopping.dao.user;

import com.mycompany.onlinefruitshopping.model.Address;
import com.mycompany.onlinefruitshopping.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    // =========================================================
    // GET ALL ADDRESSES OF ONE USER
    // =========================================================
    public List<Address> getAddressesByUserId(int userId) throws SQLException {

        List<Address> list = new ArrayList<>();

        String sql = """
                SELECT address_id,
                       user_id,
                       recipient_name,
                       phone,
                       address_line,
                       ward,
                       district,
                       province,
                       is_default
                FROM user_addresses
                WHERE user_id = ?
                ORDER BY is_default DESC, address_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Address address = new Address();

                    address.setAddressId(rs.getInt("address_id"));
                    address.setUserId(rs.getInt("user_id"));
                    address.setRecipientName(rs.getString("recipient_name"));
                    address.setPhone(rs.getString("phone"));
                    address.setAddressLine(rs.getString("address_line"));
                    address.setWard(rs.getString("ward"));
                    address.setDistrict(rs.getString("district"));
                    address.setProvince(rs.getString("province"));
                    address.setDefault(rs.getBoolean("is_default"));

                    list.add(address);
                }
            }
        }

        return list;
    }

    // =========================================================
    // GET DEFAULT ADDRESS OF USER
    // =========================================================
    public Address getDefaultAddress(int userId) throws SQLException {
        String sql = """
                SELECT TOP 1 address_id, user_id, recipient_name, phone, address_line, ward, district, province, is_default
                FROM user_addresses
                WHERE user_id = ?
                ORDER BY is_default DESC, address_id DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Address address = new Address();
                    address.setAddressId(rs.getInt("address_id"));
                    address.setUserId(rs.getInt("user_id"));
                    address.setRecipientName(rs.getString("recipient_name"));
                    address.setPhone(rs.getString("phone"));
                    address.setAddressLine(rs.getString("address_line"));
                    address.setWard(rs.getString("ward"));
                    address.setDistrict(rs.getString("district"));
                    address.setProvince(rs.getString("province"));
                    address.setDefault(rs.getBoolean("is_default"));
                    return address;
                }
            }
        }
        return null;
    }

    // =========================================================
    // GET ONE ADDRESS
    // =========================================================
    public Address getAddressById(int addressId, int userId) throws SQLException {

        String sql = """
                SELECT address_id,
                       user_id,
                       recipient_name,
                       phone,
                       address_line,
                       ward,
                       district,
                       province,
                       is_default
                FROM user_addresses
                WHERE address_id = ?
                  AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, addressId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Address address = new Address();

                    address.setAddressId(rs.getInt("address_id"));
                    address.setUserId(rs.getInt("user_id"));
                    address.setRecipientName(rs.getString("recipient_name"));
                    address.setPhone(rs.getString("phone"));
                    address.setAddressLine(rs.getString("address_line"));
                    address.setWard(rs.getString("ward"));
                    address.setDistrict(rs.getString("district"));
                    address.setProvince(rs.getString("province"));
                    address.setDefault(rs.getBoolean("is_default"));

                    return address;
                }
            }
        }

        return null;
    }

    // =========================================================
    // ADD ADDRESS
    // =========================================================
    public boolean addAddress(Address address) throws SQLException {

        String sql = """
                INSERT INTO user_addresses
                (
                    user_id,
                    recipient_name,
                    phone,
                    address_line,
                    ward,
                    district,
                    province,
                    is_default
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                if (address.isDefault()) {
                    clearDefaultAddress(conn, address.getUserId());
                }

                try (PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, address.getUserId());
                    ps.setString(2, address.getRecipientName());
                    ps.setString(3, address.getPhone());
                    ps.setString(4, address.getAddressLine());
                    ps.setString(5, address.getWard());
                    ps.setString(6, address.getDistrict());
                    ps.setString(7, address.getProvince());
                    ps.setBoolean(8, address.isDefault());

                    int rows = ps.executeUpdate();

                    conn.commit();

                    return rows > 0;
                }

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }

    // =========================================================
    // UPDATE ADDRESS
    // =========================================================
    public boolean updateAddress(Address address) throws SQLException {

        String sql = """
                UPDATE user_addresses
                SET recipient_name = ?,
                    phone = ?,
                    address_line = ?,
                    ward = ?,
                    district = ?,
                    province = ?,
                    is_default = ?
                WHERE address_id = ?
                  AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                if (address.isDefault()) {
                    clearDefaultAddress(conn, address.getUserId());
                }

                try (PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, address.getRecipientName());
                    ps.setString(2, address.getPhone());
                    ps.setString(3, address.getAddressLine());
                    ps.setString(4, address.getWard());
                    ps.setString(5, address.getDistrict());
                    ps.setString(6, address.getProvince());
                    ps.setBoolean(7, address.isDefault());
                    ps.setInt(8, address.getAddressId());
                    ps.setInt(9, address.getUserId());

                    int rows = ps.executeUpdate();

                    conn.commit();

                    return rows > 0;
                }

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }

    // =========================================================
    // DELETE ADDRESS
    // =========================================================
    public boolean deleteAddress(int addressId, int userId) throws SQLException {

        String sql = """
                DELETE FROM user_addresses
                WHERE address_id = ?
                  AND user_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, addressId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // SET DEFAULT ADDRESS
    // =========================================================
    public boolean setDefaultAddress(int addressId, int userId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                clearDefaultAddress(conn, userId);

                String sql = """
                        UPDATE user_addresses
                        SET is_default = 1
                        WHERE address_id = ?
                          AND user_id = ?
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, addressId);
                    ps.setInt(2, userId);

                    int rows = ps.executeUpdate();

                    conn.commit();

                    return rows > 0;
                }

            } catch (SQLException e) {

                conn.rollback();
                throw e;

            } finally {

                conn.setAutoCommit(true);
            }
        }
    }

    // =========================================================
    // CLEAR DEFAULT ADDRESS
    // =========================================================
    private void clearDefaultAddress(Connection conn, int userId)
            throws SQLException {

        String sql = """
                UPDATE user_addresses
                SET is_default = 0
                WHERE user_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}