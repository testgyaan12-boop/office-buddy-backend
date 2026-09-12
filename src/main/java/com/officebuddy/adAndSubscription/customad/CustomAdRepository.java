package com.officebuddy.adAndSubscription.customad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomAdRepository extends JpaRepository<CustomAd, Long> {
    List<CustomAd> findByIsActiveAndIsDeletedOrderByIdDesc(Integer isActive, Integer isDeleted);
}
