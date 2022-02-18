package server.user.service;

import server.user.domain.User;

public interface UserServicei {
    void upgradeLevels();
    void add(User user);
}
