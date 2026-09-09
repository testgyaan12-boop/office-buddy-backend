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
        return lookupRepository.findByParentLookupIdAndIsActiveAndIsDeletedOrderBySortedOrder(parent.getLookupid(), 1, 0);
    }

    public List<Lookup> getChildren(Long parentId) {
        return lookupRepository.findByParentLookupIdAndIsActiveAndIsDeletedOrderBySortedOrder(parentId, 1, 0);
    }

    public boolean existsByCodeAndParent(String code, String parentCode) {
        var parent = lookupRepository.findByLookupCode(parentCode).orElse(null);
        if (parent == null) return false;
        return lookupRepository.findByLookupCodeAndParentLookupIdAndIsActiveAndIsDeleted(code, parent.getLookupid(), 1, 0).isPresent();
    }

    public Lookup getByCode(String code) {
        return lookupRepository.findByLookupCode(code).orElse(null);
    }
}
