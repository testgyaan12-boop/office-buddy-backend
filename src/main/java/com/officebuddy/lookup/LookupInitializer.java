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
                    .isActive(true)
                    .isDeleted(false)
                    .remarks("{\"icon\":\"description\",\"color\":\"#FF6C63FF\"}")
                    .build();
            parent = lookupRepository.save(parent);
        }

        List<Lookup> toAdd = new java.util.ArrayList<>();
        if (lookupRepository.findByLookupCode("OFFER_LETTER").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("OFFER_LETTER").shortName("Offer Letter").longName("Offer Letter").parentLookupId(parent.getLookupid()).sortedOrder(1).isActive(true).isDeleted(false).remarks("{\"icon\":\"card_membership\",\"color\":\"#FF00B894\",\"eventType\":\"OFFER_RECEIVED\",\"title\":\"Received offer from \"}").build());
        if (lookupRepository.findByLookupCode("JOINING_LETTER").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("JOINING_LETTER").shortName("Joining Letter").longName("Joining Letter").parentLookupId(parent.getLookupid()).sortedOrder(2).isActive(true).isDeleted(false).remarks("{\"icon\":\"how_to_reg\",\"color\":\"#FF00ACC1\",\"eventType\":\"COMPANY_JOINED\",\"title\":\"Joined \"}").build());
        if (lookupRepository.findByLookupCode("INCREMENT_LETTER").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("INCREMENT_LETTER").shortName("Increment Letter").longName("Increment Letter").parentLookupId(parent.getLookupid()).sortedOrder(3).isActive(true).isDeleted(false).remarks("{\"icon\":\"trending_up\",\"color\":\"#FF6C63FF\",\"eventType\":\"INCREMENT\",\"title\":\"Increment at \"}").build());
        if (lookupRepository.findByLookupCode("PAYSLIP").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("PAYSLIP").shortName("Payslip").longName("Payslip").parentLookupId(parent.getLookupid()).sortedOrder(4).isActive(true).isDeleted(false).remarks("{\"icon\":\"receipt_long\",\"color\":\"#FFFDCB6E\",\"eventType\":\"PAYSLIP\",\"title\":\"Salary record at \"}").build());
        if (lookupRepository.findByLookupCode("CERTIFICATE").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("CERTIFICATE").shortName("Certificate").longName("Certificate").parentLookupId(parent.getLookupid()).sortedOrder(5).isActive(true).isDeleted(false).remarks("{\"icon\":\"verified\",\"color\":\"#FFFF6584\",\"eventType\":\"CERTIFICATE\",\"title\":\"Certificate from \"}").build());
        if (lookupRepository.findByLookupCode("RELIEVING_LETTER").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("RELIEVING_LETTER").shortName("Relieving Letter").longName("Relieving Letter").parentLookupId(parent.getLookupid()).sortedOrder(6).isActive(true).isDeleted(false).remarks("{\"icon\":\"exit_to_app\",\"color\":\"#FFE17055\",\"eventType\":\"RELIEVED\",\"title\":\"Relieved from \"}").build());
        if (lookupRepository.findByLookupCode("TDS_CERTIFICATE").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("TDS_CERTIFICATE").shortName("TDS Certificate").longName("TDS Certificate").parentLookupId(parent.getLookupid()).sortedOrder(7).isActive(true).isDeleted(false).remarks("{\"icon\":\"receipt\",\"color\":\"#FF7C4DFF\",\"eventType\":\"CERTIFICATE\",\"title\":\"TDS Certificate from \"}").build());
        if (lookupRepository.findByLookupCode("CONFIRMATION_LETTER").isEmpty())
            toAdd.add(Lookup.builder().lookupCode("CONFIRMATION_LETTER").shortName("Confirmation Letter").longName("Confirmation Letter").parentLookupId(parent.getLookupid()).sortedOrder(8).isActive(true).isDeleted(false).remarks("{\"icon\":\"task_alt\",\"color\":\"#FF26A69A\",\"eventType\":\"CONFIRMED\",\"title\":\"Confirmation at \"}").build());
        if (!toAdd.isEmpty()) lookupRepository.saveAll(toAdd);

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
