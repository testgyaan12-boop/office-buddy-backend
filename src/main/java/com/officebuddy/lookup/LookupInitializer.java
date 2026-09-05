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
        if (lookupRepository.count() > 0) return;

        var parent = Lookup.builder()
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

        List<Lookup> children = List.of(
                Lookup.builder().lookupCode("OFFER_LETTER").shortName("Offer Letter").longName("Offer Letter").parentLookupId(parent.getLookupid()).sortedOrder(1).isActive(true).isDeleted(false).remarks("{\"icon\":\"card_membership\",\"color\":\"#FF00B894\"}").build(),
                Lookup.builder().lookupCode("JOINING_LETTER").shortName("Joining Letter").longName("Joining Letter").parentLookupId(parent.getLookupid()).sortedOrder(2).isActive(true).isDeleted(false).remarks("{\"icon\":\"how_to_reg\",\"color\":\"#FF00ACC1\"}").build(),
                Lookup.builder().lookupCode("INCREMENT_LETTER").shortName("Increment Letter").longName("Increment Letter").parentLookupId(parent.getLookupid()).sortedOrder(3).isActive(true).isDeleted(false).remarks("{\"icon\":\"trending_up\",\"color\":\"#FF6C63FF\"}").build(),
                Lookup.builder().lookupCode("PAYSLIP").shortName("Payslip").longName("Payslip").parentLookupId(parent.getLookupid()).sortedOrder(4).isActive(true).isDeleted(false).remarks("{\"icon\":\"receipt_long\",\"color\":\"#FFFDCB6E\"}").build(),
                Lookup.builder().lookupCode("CERTIFICATE").shortName("Certificate").longName("Certificate").parentLookupId(parent.getLookupid()).sortedOrder(5).isActive(true).isDeleted(false).remarks("{\"icon\":\"verified\",\"color\":\"#FFFF6584\"}").build(),
                Lookup.builder().lookupCode("RELIEVING_LETTER").shortName("Relieving Letter").longName("Relieving Letter").parentLookupId(parent.getLookupid()).sortedOrder(6).isActive(true).isDeleted(false).remarks("{\"icon\":\"exit_to_app\",\"color\":\"#FFE17055\"}").build()
        );
        lookupRepository.saveAll(children);
    }
}
