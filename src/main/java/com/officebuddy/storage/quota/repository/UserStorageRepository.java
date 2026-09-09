package com.officebuddy.storage.quota.repository;

import com.officebuddy.storage.quota.entity.UserStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserStorageRepository extends JpaRepository<UserStorage, Long> {
    Optional<UserStorage> findByUserId(UUID userId);
}
