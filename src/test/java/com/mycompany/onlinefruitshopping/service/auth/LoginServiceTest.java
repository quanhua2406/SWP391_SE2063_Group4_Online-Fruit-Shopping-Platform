package com.mycompany.onlinefruitshopping.service.auth;

import com.mycompany.onlinefruitshopping.dao.auth.UserDAO;
import com.mycompany.onlinefruitshopping.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.mycompany.onlinefruitshopping.service.auth.LoginService.Status.*;

class LoginServiceTest {
    @Test void invalidInputNeverQueriesDao() {
        RecordingDAO dao = new RecordingDAO(); LoginService service = new LoginService(dao);
        assertEquals(INVALID_CREDENTIALS, service.login(" ", "secret", "Customer").getStatus());
        assertEquals(INVALID_CREDENTIALS, service.login("a@b.com", null, "Customer").getStatus());
        assertEquals(INVALID_CREDENTIALS, service.login("a@b.com", "", "Customer").getStatus());
        assertEquals(0, dao.calls);
    }
    @Test void emailIsTrimmedButPasswordIsNot() {
        RecordingDAO dao = new RecordingDAO(); LoginService service = new LoginService(dao);
        assertEquals(SUCCESS, service.login(" a@b.com ", " secret ", "Customer").getStatus());
        assertEquals("a@b.com", dao.email); assertEquals(1, dao.calls);
        assertEquals(INVALID_CREDENTIALS, service.login("a@b.com", "secret", "Customer").getStatus());
    }
    @Test void lockedAccountAndWrongRoleAreRejected() {
        RecordingDAO dao = new RecordingDAO(); LoginService service = new LoginService(dao);
        dao.user.setStatus("locked");
        assertEquals(ACCOUNT_LOCKED, service.login("a@b.com", " secret ", "Customer").getStatus());
        dao.user.setStatus("active");
        assertEquals(ROLE_MISMATCH, service.login("a@b.com", " secret ", "ShopOwner").getStatus());
    }
    private static class RecordingDAO extends UserDAO {
        int calls; String email; final User user = new User();
        RecordingDAO() {
            user.setUserID(10); user.setRoleName("Customer"); user.setStatus("active");
            user.setPasswordHash(BCrypt.hashpw(" secret ", BCrypt.gensalt(4)));
        }
        @Override public User findByEmail(String email) { calls++; this.email = email; return user; }
    }
}
