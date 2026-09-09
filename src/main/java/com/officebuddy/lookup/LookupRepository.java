package com.officebuddy.lookup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LookupRepository extends JpaRepository<Lookup, Long> {
    List<Lookup> findByParentLookupIdAndIsActiveAndIsDeletedOrderBySortedOrder(Long parentLookupId, Integer isActive, Integer isDeleted);
    List<Lookup> findByLookupCodeAndIsActiveAndIsDeleted(String lookupCode, Integer isActive, Integer isDeleted);
    Optional<Lookup> findByLookupCodeAndParentLookupIdAndIsActiveAndIsDeleted(String lookupCode, Long parentLookupId, Integer isActive, Integer isDeleted);
    Optional<Lookup> findByLookupCode(String lookupCode);
    List<Lookup> findByIsActiveAndIsDeletedOrderBySortedOrder(Integer isActive, Integer isDeleted);
}
