package com.officebuddy.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String verificationToken);
    boolean existsByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByAccountLockedUntilIsNotNullAndAccountLockedUntilBefore(LocalDateTime time);

    long countByAccessRole(String accessRole);

    long countByAccountLockedUntilAfter(LocalDateTime time);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countCreatedSince(@Param("since") Date since);

    @Query("SELECT FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM'), COUNT(u) FROM User u GROUP BY FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM') ORDER BY FUNCTION('TO_CHAR', u.createdAt, 'YYYY-MM')")
    List<Object[]> countByMonth();

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (:q IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))) AND (:from IS NULL OR u.createdAt >= :from) AND (:to IS NULL OR u.createdAt <= :to)")
    Page<User> searchAdmin(@Param("q") String q, @Param("from") Date from, @Param("to") Date to, Pageable pageable);
}
