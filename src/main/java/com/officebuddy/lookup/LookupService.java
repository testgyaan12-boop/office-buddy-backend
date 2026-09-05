package com.officebuddy.lookup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LookupService {

    private final LookupRepository lookupRepository;

    public List<Lookup> getByParentCode(String parentCode) {
        var parent = lookupRepository.findByLookupCode(parentCode).orElse(null);
        if (parent == null) return List.of();
        return lookupRepository.findByParentLookupIdAndIsActiveTrueAndIsDeletedFalseOrderBySortedOrder(parent.getLookupid());
    }

    public List<Lookup> getChildren(Long parentId) {
        return lookupRepository.findByParentLookupIdAndIsActiveTrueAndIsDeletedFalseOrderBySortedOrder(parentId);
    }

    public boolean existsByCodeAndParent(String code, String parentCode) {
        var parent = lookupRepository.findByLookupCode(parentCode).orElse(null);
        if (parent == null) return false;
        return lookupRepository.findByLookupCodeAndParentLookupIdAndIsActiveTrueAndIsDeletedFalse(code, parent.getLookupid()).isPresent();
    }

    public Lookup getByCode(String code) {
        return lookupRepository.findByLookupCode(code).orElse(null);
    }
}
