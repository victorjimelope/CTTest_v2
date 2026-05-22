package com.cttest.service;

import com.cttest.model.entity.User;

public interface UserService extends BaseService<User, Integer> {
    User save(User user, boolean encodePassword);
}
