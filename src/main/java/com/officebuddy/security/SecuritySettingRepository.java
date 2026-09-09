package com.officebuddy.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecuritySettingRepository extends JpaRepository<SecuritySetting, Long> {
    Optional<SecuritySetting> findByConfigKey(String configKey);
    List<SecuritySetting> findByIsActiveAndIsDeleted(Integer isActive, Integer isDeleted);
}
