package org.example.factory;

import org.example.model.User;

public class UserFactory {

    public User create(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}