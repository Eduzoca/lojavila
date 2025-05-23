package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private UserDAO dao = new UserDAO();

    public User login(String username, String password) throws SQLException {
        User u = dao.findByUsername(username);
        if (u != null && PasswordUtil.checkPassword(password, u.getPasswordHash())) {
            return u;
        }
        return null;
    }
}
