package com.officebuddy.adAndSubscription.invoice;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.officebuddy.adAndSubscription.subscription.entity.Subscription;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository repo;

    public Invoice createForSubscription(User user, Subscription sub, String paymentId, long amount, String currency) {
        if (paymentId != null) {
            var dup = repo.findByRazorpayPaymentId(paymentId).orElse(null);
            if (dup != null) return dup;
        }
        String invoiceNo = "INV-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        var inv = Invoice.builder()
                .userId(user.getId())
                .subscriptionId(sub.getId())
                .invoiceNo(invoiceNo)
                .planCode(sub.getPlanCode())
                .planName(sub.getPlanName())
                .amount(amount)
                .currency(currency != null ? currency : "INR")
                .razorpayOrderId(sub.getRazorpayOrderId())
                .razorpayPaymentId(paymentId)
                .status("PAID")
                .issuedAt(LocalDateTime.now())
                .build();
        return repo.save(inv);
    }

    public List<Invoice> listForUser(UUID userId) {
        return repo.findByUserIdOrderByIssuedAtDesc(userId);
    }

    public Invoice getForUser(UUID userId, UUID invoiceId) {
        var inv = repo.findById(invoiceId).orElseThrow(() -> new RuntimeException("Invoice not found"));
        if (!inv.getUserId().equals(userId)) throw new RuntimeException("Access denied");
        return inv;
    }

    public byte[] renderPdf(Invoice inv, String userName, String userEmail) {
        try (var out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            doc.add(new Paragraph("OfficeBuddy - Payment Invoice", title));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Invoice No: " + inv.getInvoiceNo(), bold));
            doc.add(new Paragraph("Issued: " + (inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : ""), normal));
            doc.add(new Paragraph("Status: " + inv.getStatus(), normal));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Billed To", bold));
            doc.add(new Paragraph((userName != null ? userName : "") + " (" + (userEmail != null ? userEmail : "") + ")", normal));
            doc.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(2);
            table.setWidths(new float[]{3, 2});
            table.addCell("Item");
            table.addCell("Amount");
            double amount = inv.getAmount() != null ? inv.getAmount() : 0;
            table.addCell((inv.getPlanName() != null ? inv.getPlanName() : inv.getPlanCode()) + " subscription");
            table.addCell(String.format("%s %.2f", inv.getCurrency() != null ? inv.getCurrency() : "INR", amount));
            doc.add(table);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Payment ID: " + (inv.getRazorpayPaymentId() != null ? inv.getRazorpayPaymentId() : "-"), normal));
            doc.add(new Paragraph("Order ID: " + (inv.getRazorpayOrderId() != null ? inv.getRazorpayOrderId() : "-"), normal));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Thank you for your purchase!", normal));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.warn("Invoice PDF render failed for {}: {}", inv.getInvoiceNo(), e.getMessage());
            throw new RuntimeException("Failed to render invoice PDF");
        }
    }
}
