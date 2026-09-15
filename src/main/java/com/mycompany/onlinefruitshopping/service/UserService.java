package com.mycompany.onlinefruitshopping.service;

import com.mycompany.onlinefruitshopping.dao.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public boolean registerUser(
            String fullName,
            String email,
            String password,
            String phone,
            String address)
            throws SQLException {

        // Kiểm tra email đã tồn tại
        if (userDAO.emailExists(email)) {
            return false;
        }

        // Hash password bằng BCrypt
        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        return userDAO.registerUser(
                fullName,
                email,
                passwordHash,
                phone,
                address
        );
    }
    public boolean isEmailExists(String email) throws SQLException {
    return userDAO.emailExists(email);
}

    public User getUserByLogin(
            String email,
            String password)
            throws SQLException {

        User user = userDAO.getUserByEmail(email);

        if (user == null) {
            return null;
        }

        // Kiểm tra password nhập vào với password hash trong DB
        if (BCrypt.checkpw(
                password,
                user.getPasswordHash()
        )) {
            return user;
        }

        return null;
    }
}