package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class UserService {

    private final UserDAO userDAO = new UserDAO();


    // =========================================================
    // REGISTER USER
    // =========================================================

    public boolean registerUser(
            String fullName,
            String email,
            String password,
            String phone,
            String address)
            throws SQLException {

        // Check email already exists
        if (userDAO.emailExists(email)) {
            return false;
        }

        // Hash password
        String passwordHash =
                BCrypt.hashpw(
                        password,
                        BCrypt.gensalt(12)
                );

        // Save user
        return userDAO.registerUser(
                fullName,
                email,
                passwordHash,
                phone,
                address
        );
    }


    // =========================================================
    // CHECK EMAIL
    // =========================================================

    public boolean isEmailExists(String email)
            throws SQLException {

        return userDAO.emailExists(email);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    // =========================================================
// LOGIN
// =========================================================

public User getUserByLogin(
        String email,
        String password)
        throws SQLException {

    // Get account including locked accounts
    User user =
            userDAO.getUserByEmailForLogin(email);

    // Account not found
    if (user == null) {
        return null;
    }

    // Check password
    if (!BCrypt.checkpw(
            password,
            user.getPasswordHash())) {

        return null;
    }

    return user;
}

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

    public boolean updateProfile(
            int userId,
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