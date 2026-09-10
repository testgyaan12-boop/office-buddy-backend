package com.officebuddy.lookup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LookupInitializer {

    private final LookupRepository lookupRepository;

    @PostConstruct
    public void init() {
        var parent = lookupRepository.findByLookupCode("DOC_TYPE").orElse(null);
        if (parent == null) {
            parent = Lookup.builder()
                    .lookupCode("DOC_TYPE")
                    .shortName("Document Type")
                    .longName("Document Type")
                    .parentLookupId(null)
                    .sortedOrder(1)
                    .build();
            parent.setRemarks("{\"icon\":\"description\",\"color\":\"#FF6C63FF\"}");
            parent = lookupRepository.save(parent);
        }

        List<Lookup> toAdd = new java.util.ArrayList<>();
        addLookup(toAdd, "OFFER_LETTER", "Offer Letter", "Offer Letter", parent.getLookupid(), 1, "{\"icon\":\"card_membership\",\"color\":\"#FF00B894\",\"eventType\":\"OFFER_RECEIVED\",\"title\":\"Received offer from \"}");
        addLookup(toAdd, "JOINING_LETTER", "Joining Letter", "Joining Letter", parent.getLookupid(), 2, "{\"icon\":\"how_to_reg\",\"color\":\"#FF00ACC1\",\"eventType\":\"COMPANY_JOINED\",\"title\":\"Joined \"}");
        addLookup(toAdd, "INCREMENT_LETTER", "Increment Letter", "Increment Letter", parent.getLookupid(), 3, "{\"icon\":\"trending_up\",\"color\":\"#FF6C63FF\",\"eventType\":\"INCREMENT\",\"title\":\"Increment at \"}");
        addLookup(toAdd, "PAYSLIP", "Payslip", "Payslip", parent.getLookupid(), 4, "{\"icon\":\"receipt_long\",\"color\":\"#FFFDCB6E\",\"eventType\":\"PAYSLIP\",\"title\":\"Salary record at \"}");
        addLookup(toAdd, "CERTIFICATE", "Certificate", "Certificate", parent.getLookupid(), 5, "{\"icon\":\"verified\",\"color\":\"#FFFF6584\",\"eventType\":\"CERTIFICATE\",\"title\":\"Certificate from \"}");
        addLookup(toAdd, "RELIEVING_LETTER", "Relieving Letter", "Relieving Letter", parent.getLookupid(), 6, "{\"icon\":\"exit_to_app\",\"color\":\"#FFE17055\",\"eventType\":\"RELIEVED\",\"title\":\"Relieved from \"}");
        addLookup(toAdd, "TDS_CERTIFICATE", "TDS Certificate", "TDS Certificate", parent.getLookupid(), 7, "{\"icon\":\"receipt\",\"color\":\"#FF7C4DFF\",\"eventType\":\"CERTIFICATE\",\"title\":\"TDS Certificate from \"}");
        addLookup(toAdd, "CONFIRMATION_LETTER", "Confirmation Letter", "Confirmation Letter", parent.getLookupid(), 8, "{\"icon\":\"task_alt\",\"color\":\"#FF26A69A\",\"eventType\":\"CONFIRMED\",\"title\":\"Confirmation at \"}");
        if (!toAdd.isEmpty()) lookupRepository.saveAll(toAdd);

        backfillEventTypes();
    }

    private void addLookup(List<Lookup> toAdd, String code, String shortName, String longName, Long parentId, int order, String remarks) {
        if (lookupRepository.findByLookupCode(code).isEmpty()) {
            var lookup = Lookup.builder()
                    .lookupCode(code)
                    .shortName(shortName)
                    .longName(longName)
                    .parentLookupId(parentId)
                    .sortedOrder(order)
                    .build();
            lookup.setRemarks(remarks);
            toAdd.add(lookup);
        }
    }

    private void backfillEventTypes() {
        // Update existing lookups to ensure eventType/title are present (for DBs created before this fix)
        for (String code : new String[]{"OFFER_LETTER","JOINING_LETTER","INCREMENT_LETTER","PAYSLIP","CERTIFICATE","RELIEVING_LETTER","TDS_CERTIFICATE","CONFIRMATION_LETTER"}) {
            var existing = lookupRepository.findByLookupCode(code).orElse(null);
            if (existing != null && (existing.getRemarks() == null || !existing.getRemarks().contains("eventType"))) {
                String eventType = switch (code) {
                    case "OFFER_LETTER" -> "OFFER_RECEIVED";
                    case "JOINING_LETTER" -> "COMPANY_JOINED";
                    case "INCREMENT_LETTER" -> "INCREMENT";
                    case "PAYSLIP" -> "PAYSLIP";
                    case "CERTIFICATE", "TDS_CERTIFICATE" -> "CERTIFICATE";
                    case "RELIEVING_LETTER" -> "RELIEVED";
                    case "CONFIRMATION_LETTER" -> "CONFIRMED";
                    default -> "DOCUMENT_UPLOADED";
                };
                String title = switch (code) {
                    case "OFFER_LETTER" -> "Received offer from ";
                    case "JOINING_LETTER" -> "Joined ";
                    case "INCREMENT_LETTER" -> "Increment at ";
                    case "PAYSLIP" -> "Salary record at ";
                    case "CERTIFICATE" -> "Certificate from ";
                    case "RELIEVING_LETTER" -> "Relieved from ";
                    case "TDS_CERTIFICATE" -> "TDS Certificate from ";
                    case "CONFIRMATION_LETTER" -> "Confirmation at ";
                    default -> "Document uploaded for ";
                };
                String current = existing.getRemarks() != null ? existing.getRemarks() : "{}";
                // Merge eventType and title into existing JSON
                if (!current.contains("eventType")) {
                    String updated = current.replace("}", ",\"eventType\":\"" + eventType + "\",\"title\":\"" + title + "\"}");
                    // Fix double braces if original was {}
                    if (updated.startsWith("{,")) updated = "{" + updated.substring(2);
                    updated = updated.replace(",,", ",");
                    existing.setRemarks(updated);
                    lookupRepository.save(existing);
                }
            }
        }
    }
}
