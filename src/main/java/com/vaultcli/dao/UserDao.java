package com.vaultcli.dao;

import com.vaultcli.model.User;

public interface UserDao {
    boolean createUser(User user);
    User getUserByUsername(String username);
    boolean userExists(String username);
}
