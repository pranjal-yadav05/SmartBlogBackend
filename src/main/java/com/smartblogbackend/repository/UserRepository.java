package com.smartblogbackend.repository;

import com.smartblogbackend.model.User;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Enhanced search with pagination
    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
    
    // Get users by initial letter of name (for alphabetical search)
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(:initial || '%')")
    Page<User> findByNameStartingWithIgnoreCase(@Param("initial") String initial, Pageable pageable);
}
