package com.officebuddy.adAndSubscription.invoice;

import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @GetMapping("/me")
    public ResponseEntity<List<Map<String, Object>>> mine(Authentication auth) {
        var user = (User) auth.getPrincipal();
        var list = service.listForUser(user.getId()).stream().map(inv -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", inv.getId().toString());
            m.put("invoiceNo", inv.getInvoiceNo());
            m.put("planCode", inv.getPlanCode());
            m.put("planName", inv.getPlanName());
            m.put("amountPaise", inv.getAmountPaise());
            m.put("currency", inv.getCurrency());
            m.put("status", inv.getStatus());
            m.put("razorpayPaymentId", inv.getRazorpayPaymentId());
            m.put("issuedAt", inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(Authentication auth, @PathVariable("id") UUID id) {
        var user = (User) auth.getPrincipal();
        var inv = service.getForUser(user.getId(), id);
        byte[] pdf = service.renderPdf(inv, user.getName(), user.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(inv.getInvoiceNo() + ".pdf", StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
