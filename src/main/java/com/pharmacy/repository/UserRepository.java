package com.pharmacy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pharmacy.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    // total users
    long count();
}

