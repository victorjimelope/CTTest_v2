package com.cttest.service.impl;

import com.cttest.model.entity.User;
import com.cttest.repository.UserRepository;
import com.cttest.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseServiceImpl<User, Integer, UserRepository> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository, "User");
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> search(String query, Pageable pageable) {
        return repository.findAll((root, cq, cb) -> {
            Predicate usernameLike = cb.like(cb.lower(root.get("username")), "%" + query.toLowerCase() + "%");
            return cb.or(usernameLike);
        }, pageable);
    }

    @Override
    @Transactional
    public User save(User user, boolean encodePassword) {
        if (encodePassword && user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return repository.save(user);
    }

    @Override
    @Transactional
    public User save(User user) {
        return save(user, false);
    }

    @Override
    @Transactional
    public User update(User user) {
        if (user.getNewPassword() != null && !user.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getNewPassword()));
        }
        return repository.save(user);
    }

}
