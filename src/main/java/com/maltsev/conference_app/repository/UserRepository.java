package com.maltsev.conference_app.repository;

import com.maltsev.conference_app.model.User;
import com.maltsev.conference_app.model.Role;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailLocalAndEmailDomain(String emailLocal, String emailDomain);

    List<User> findByRole(Role role, Sort sort);
}
