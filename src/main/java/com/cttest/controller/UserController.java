package com.cttest.controller;

import com.cttest.model.entity.User;
import com.cttest.model.dto.EntityDTOMapper;
import com.cttest.model.dto.UserDTO;
import com.cttest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, UserDTO, UserService> {

    public UserController(UserService service) {
        super(service);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return listAll(search, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody User user) {
        User saved = service.save(user, true);
        return ResponseEntity.created(
                java.net.URI.create("/api/users/" + saved.getId())
        ).body(toDto(saved));
    }

    @Override
    protected UserDTO toDto(User entity) {
        return EntityDTOMapper.toDTO(entity);
    }

    @Override
    protected String getPathSegment() {
        return "users";
    }
}
