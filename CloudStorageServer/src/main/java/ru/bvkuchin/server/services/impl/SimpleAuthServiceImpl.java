package ru.bvkuchin.server.services.impl;

import ru.bvkuchin.server.models.User;
import ru.bvkuchin.server.services.AuthService;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthServiceImpl implements AuthService {
    private static List<User> users = new ArrayList<>();
    static{
        users.add(new User("boris", "123456"));
        users.add(new User("anna", "123456"));
        users.add(new User("dima", "123456"));

    }

    @Override
    public void addUser(String login, String password) {
        users.add(new User(login, password));

    }

    @Override
    public boolean checkCredentials(String login, String password) {
        for (User u : users) {
            if (u.getLogin().equals(login.toLowerCase()) && u.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkUserExist(String login) {
        for (User u : users) {
            if (u.getLogin().toLowerCase().equals(login.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> getUserList() {
        return users;
    }
}
