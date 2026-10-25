package com.maltsev.conference_app.service;

import com.maltsev.conference_app.dto.UserResponse;
import com.maltsev.conference_app.model.Role;
import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private static final Sort ORDER = Sort.by("lastName", "firstName", "id");

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Role role) {
        List<User> users = role == null ? userRepository.findAll(ORDER) : userRepository.findByRole(role, ORDER);
        return users.stream().map(UserResponse::from).toList();
    }
}
