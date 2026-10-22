package com.mycompany.onlinefruitshopping.service.user;

import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;

import java.sql.SQLException;

/**
 * User profile operations (read / update).
 *
 * <p>Authentication concerns live in
 * {@code service.auth.LoginService} and registration concerns in
 * {@code service.auth.RegistrationService}.
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    public User getUserByEmail(String email)
            throws SQLException {

        return userDAO.getUserByEmail(email);
    }

    // =========================================================
    // GET USER BY ID
    // =========================================================

    public User getUserById(int userId)
            throws SQLException {

        return userDAO.getUserById(userId);
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    public boolean updateProfile(int userId,
                                 String fullName,
                                 String phone)
            throws SQLException {

        return userDAO.updateProfile(
                userId,
                fullName,
                phone
        );
    }
}
