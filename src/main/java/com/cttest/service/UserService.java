package com.cttest.service;

import com.cttest.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    Page<User> search(String query, Pageable pageable);
    User findById(Integer id);
    User save(User user, boolean encodePassword);
    User update(User user);
    void deleteById(Integer id);
}
