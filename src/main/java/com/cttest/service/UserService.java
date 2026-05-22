package com.cttest.service;

import com.cttest.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    Page<User> search(String query, Pageable pageable);
    User findById(Integer id);
    User save(User user);
    User update(User user);
    void deleteById(Integer id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
