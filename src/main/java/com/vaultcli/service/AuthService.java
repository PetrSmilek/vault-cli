package com.vaultcli.service;

import org.mindrot.jbcrypt.BCrypt;
import com.vaultcli.model.User;
import com.vaultcli.dao.UserDao;
import com.vaultcli.dao.impl.UserDaoImpl;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthService() {
        this(new UserDaoImpl());
    }

    public boolean registerUser(String username, String password) {
        if (userDao.userExists(username)) {
            System.err.println("Uživatel již existuje!");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(username, hashedPassword);
        return userDao.createUser(newUser);
    }

    public User authenticate(String username, String password) {
        User user = userDao.getUserByUsername(username);
        if (user == null) {
            System.err.println("Uživatel neexistuje!");
            return null;
        }

        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        } else {
            System.err.println("Neplatné heslo!");
            return null;
        }
    }
}