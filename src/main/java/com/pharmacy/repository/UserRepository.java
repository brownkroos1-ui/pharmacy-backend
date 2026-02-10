package com.pharmacy.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pharmacy.model.User;
import com.pharmacy.model.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByRole(Role role);
    // total users
    long count();

    @Query("""
        SELECT u FROM User u
        WHERE (:query IS NULL OR :query = ''
           OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:role IS NULL OR u.role = :role)
          AND (:active IS NULL OR u.active = :active)
        ORDER BY u.id DESC
    """)
    List<User> searchUsers(
            @Param("query") String query,
            @Param("role") Role role,
            @Param("active") Boolean active
    );
}

