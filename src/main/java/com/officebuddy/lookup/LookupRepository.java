package com.officebuddy.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupRepository extends JpaRepository<Lookup, Long> {
    List<Lookup> findByParentLookupIdAndIsActiveTrueAndIsDeletedFalseOrderBySortedOrder(Long parentLookupId);
    List<Lookup> findByLookupCodeAndIsActiveTrueAndIsDeletedFalse(String lookupCode);
    Optional<Lookup> findByLookupCodeAndParentLookupIdAndIsActiveTrueAndIsDeletedFalse(String lookupCode, Long parentLookupId);
    Optional<Lookup> findByLookupCode(String lookupCode);
    List<Lookup> findByIsActiveTrueAndIsDeletedFalseOrderBySortedOrder();
}
