package com.officebuddy.admin;

import com.officebuddy.adAndSubscription.invoice.InvoiceRepository;
import com.officebuddy.adAndSubscription.provider.entity.AdProvider;
import com.officebuddy.adAndSubscription.provider.repository.AdProviderRepository;
import com.officebuddy.adAndSubscription.config.entity.AdConfig;
import com.officebuddy.adAndSubscription.config.repository.AdConfigRepository;
import com.officebuddy.adAndSubscription.customad.CustomAd;
import com.officebuddy.adAndSubscription.customad.CustomAdRepository;
import com.officebuddy.adAndSubscription.subscription.entity.Subscription;
import com.officebuddy.adAndSubscription.subscription.plan.entity.Plan;
import com.officebuddy.adAndSubscription.subscription.plan.repository.PlanRepository;
import com.officebuddy.adAndSubscription.subscription.repository.SubscriptionRepository;
import com.officebuddy.company.CompanyRepository;
import com.officebuddy.document.DocumentRepository;
import com.officebuddy.goal.GoalRepository;
import com.officebuddy.jobswitch.JobSwitchPackDownloadDetailsRepository;
import com.officebuddy.todo.TodoRepository;
import com.officebuddy.lookup.Lookup;
import com.officebuddy.lookup.LookupRepository;
import com.officebuddy.payment.PaymentConfig;
import com.officebuddy.payment.PaymentConfigRepository;
import com.officebuddy.reminder.entity.Reminder;
import com.officebuddy.reminder.repository.ReminderRepository;
import com.officebuddy.security.SecuritySetting;
import com.officebuddy.security.SecuritySettingRepository;
import com.officebuddy.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final DocumentRepository documentRepository;
    private final GoalRepository goalRepository;
    private final TodoRepository todoRepository;
    private final JobSwitchPackDownloadDetailsRepository packDownloadDetailsRepository;
    private final CompanyRepository companyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PlanRepository planRepository;
    private final LookupRepository lookupRepository;
    private final ReminderRepository reminderRepository;
    private final AdProviderRepository adProviderRepository;
    private final AdConfigRepository adConfigRepository;
    private final CustomAdRepository customAdRepository;
    private final SecuritySettingRepository securitySettingRepository;
    private final PaymentConfigRepository paymentConfigRepository;
    private final UserRepository userRepository;

    // ---- documents ----
    @GetMapping("/documents")
    public ResponseEntity<?> documents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<?> page;
        if (query == null) {
            if (userId != null) page = documentRepository.findByUserId(userId, pageable);
            else page = documentRepository.findAll(pageable);
        } else {
            page = documentRepository.searchAdmin(userId, query, pageable);
        }
        var content = page.getContent();
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : content) {
            var d = (com.officebuddy.document.Document) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", d.getId().toString());
            m.put("userId", d.getUserId() != null ? d.getUserId().toString() : null);
            m.put("title", d.getTitle());
            m.put("fileName", d.getFileName());
            m.put("type", d.getType());
            m.put("fileSize", d.getFileSize());
            m.put("mimeType", d.getMimeType());
            m.put("companyId", d.getCompanyId() != null ? d.getCompanyId().toString() : null);
            m.put("uploadedAt", d.getUploadedAt() != null ? d.getUploadedAt().toString() : null);
            m.put("isActive", d.getIsActive());
            if (d.getUserId() != null) {
                var user = userRepository.findById(d.getUserId()).orElse(null);
                if (user != null) {
                    m.put("userName", user.getName());
                    m.put("userEmail", user.getEmail());
                }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<?> document(@PathVariable UUID id) {
        return ResponseEntity.ok(documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found")));
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var doc = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) doc.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) doc.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        return ResponseEntity.ok(documentRepository.save(doc));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable UUID id) {
        documentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Document deleted"));
    }

    // ---- companies ----
    @GetMapping("/companies")
    public ResponseEntity<?> companies(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @PageableDefault(size = 20) Pageable pageable) {
        java.time.LocalDate f = parseDate(from);
        java.time.LocalDate t = parseDate(to);
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<?> page;
        if (query == null && f == null && t == null) {
            if (userId != null) page = companyRepository.findByUserId(userId, pageable);
            else page = companyRepository.findAll(pageable);
        } else {
            page = companyRepository.searchAdmin(userId, query, f, t, pageable);
        }
        var content = page.getContent();
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : content) {
            var c = (com.officebuddy.company.Company) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", c.getId().toString());
            m.put("userId", c.getUserId() != null ? c.getUserId().toString() : null);
            m.put("name", c.getName());
            m.put("role", c.getRole());
            m.put("startDate", c.getStartDate() != null ? c.getStartDate().toString() : null);
            m.put("endDate", c.getEndDate() != null ? c.getEndDate().toString() : null);
            m.put("isCurrent", c.isCurrent());
            m.put("isActive", c.getIsActive());
            if (c.getUserId() != null) {
                var user = userRepository.findById(c.getUserId()).orElse(null);
                if (user != null) {
                    m.put("userName", user.getName());
                    m.put("userEmail", user.getEmail());
                }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    private java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(s.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<?> company(@PathVariable UUID id) {
        return ResponseEntity.ok(companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found")));
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var company = companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) company.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) company.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        return ResponseEntity.ok(companyRepository.save(company));
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Map<String, String>> deleteCompany(@PathVariable UUID id) {
        companyRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Company deleted"));
    }

    // ---- subscriptions ----
    @GetMapping("/subscriptions")
    public ResponseEntity<?> subscriptions(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<?> page;
        if (query == null) {
            if (userId != null) page = subscriptionRepository.findByUserId(userId, pageable);
            else page = subscriptionRepository.findAll(pageable);
        } else {
            page = subscriptionRepository.searchAdmin(userId, query, pageable);
        }
        var content = page.getContent();
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : content) {
            var s = (Subscription) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", s.getId().toString());
            m.put("userId", s.getUserId() != null ? s.getUserId().toString() : null);
            m.put("planCode", s.getPlanCode());
            m.put("planName", s.getPlanName());
            m.put("storageLimitBytes", s.getStorageLimitBytes());
            m.put("maxCompanies", s.getMaxCompanies());
            m.put("maxDocuments", s.getMaxDocuments());
            m.put("adsEnabled", s.getAdsEnabled());
            m.put("status", s.getStatus());
            m.put("startDate", s.getStartDate() != null ? s.getStartDate().toString() : null);
            m.put("expiryDate", s.getExpiryDate() != null ? s.getExpiryDate().toString() : null);
            m.put("isActive", s.getIsActive());
            if (s.getUserId() != null) {
                var user = userRepository.findById(s.getUserId()).orElse(null);
                if (user != null) {
                    m.put("userName", user.getName());
                    m.put("userEmail", user.getEmail());
                }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<Subscription> subscription(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found")));
    }

    @PutMapping("/subscriptions/{id}")
    public ResponseEntity<?> updateSubscription(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var sub = subscriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.containsKey("status")) sub.setStatus(String.valueOf(body.get("status")));
        if (body.containsKey("planCode")) sub.setPlanCode(String.valueOf(body.get("planCode")));
        if (body.containsKey("planName")) sub.setPlanName(String.valueOf(body.get("planName")));
        if (body.containsKey("storageLimitBytes")) sub.setStorageLimitBytes(Long.valueOf(String.valueOf(body.get("storageLimitBytes"))));
        if (body.containsKey("maxCompanies")) sub.setMaxCompanies(Integer.valueOf(String.valueOf(body.get("maxCompanies"))));
        if (body.containsKey("maxDocuments")) sub.setMaxDocuments(Integer.valueOf(String.valueOf(body.get("maxDocuments"))));
        if (body.containsKey("adsEnabled")) sub.setAdsEnabled(Boolean.valueOf(String.valueOf(body.get("adsEnabled"))));
        if (body.containsKey("isActive")) sub.setIsActive(Integer.valueOf(String.valueOf(body.get("isActive"))));
        if (body.containsKey("expiryDate")) {
            String v = String.valueOf(body.get("expiryDate"));
            if (v != null && !v.isBlank() && !"null".equals(v)) sub.setExpiryDate(LocalDateTime.parse(v));
        }
        subscriptionRepository.save(sub);
        return ResponseEntity.ok(sub);
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<?> deleteSubscription(@PathVariable UUID id) {
        subscriptionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ---- invoices ----
    @GetMapping("/invoices")
    public ResponseEntity<?> invoices(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<?> page;
        if (query == null) {
            if (userId != null) page = invoiceRepository.findByUserId(userId, pageable);
            else page = invoiceRepository.findAll(pageable);
        } else {
            page = invoiceRepository.searchAdmin(userId, query, pageable);
        }
        var content = page.getContent();
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : content) {
            var inv = (com.officebuddy.adAndSubscription.invoice.Invoice) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", inv.getId().toString());
            m.put("userId", inv.getUserId() != null ? inv.getUserId().toString() : null);
            m.put("invoiceNo", inv.getInvoiceNo());
            m.put("planCode", inv.getPlanCode());
            m.put("planName", inv.getPlanName());
            m.put("amount", inv.getAmount());
            m.put("currency", inv.getCurrency());
            m.put("status", inv.getStatus());
            m.put("issuedAt", inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : null);
            m.put("isActive", inv.getIsActive());
            if (inv.getUserId() != null) {
                var user = userRepository.findById(inv.getUserId()).orElse(null);
                if (user != null) {
                    m.put("userName", user.getName());
                    m.put("userEmail", user.getEmail());
                }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @PutMapping("/invoices/{id}")
    public ResponseEntity<?> updateInvoice(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var inv = invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.containsKey("status")) inv.setStatus(String.valueOf(body.get("status")));
        if (body.containsKey("planName")) inv.setPlanName(String.valueOf(body.get("planName")));
        if (body.containsKey("planCode")) inv.setPlanCode(String.valueOf(body.get("planCode")));
        if (body.containsKey("amount")) inv.setAmount(Long.valueOf(String.valueOf(body.get("amount"))));
        if (body.containsKey("currency")) inv.setCurrency(String.valueOf(body.get("currency")));
        if (body.containsKey("isActive")) inv.setIsActive(Integer.valueOf(String.valueOf(body.get("isActive"))));
        invoiceRepository.save(inv);
        return ResponseEntity.ok(inv);
    }

    @DeleteMapping("/invoices/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable UUID id) {
        invoiceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ---- plans ----
    @GetMapping("/plans")
    public ResponseEntity<?> plans(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) return ResponseEntity.ok(planRepository.findAll());
        return ResponseEntity.ok(planRepository.findByPlanNameContainingIgnoreCaseOrPlanCodeContainingIgnoreCase(q.trim(), q.trim()));
    }

    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        plan.setId(null);
        return ResponseEntity.ok(planRepository.save(plan));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var plan = planRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("planName") != null) plan.setPlanName(body.get("planName").toString());
        if (body.get("period") != null) plan.setPeriod(body.get("period").toString());
        if (body.get("allocatedBytes") != null) plan.setAllocatedBytes(Long.parseLong(body.get("allocatedBytes").toString()));
        if (body.get("allocatedUnit") != null) plan.setAllocatedUnit(body.get("allocatedUnit").toString());
        if (body.get("amount") != null) plan.setAmount(Long.parseLong(body.get("amount").toString()));
        if (body.get("currency") != null) plan.setCurrency(body.get("currency").toString());
        if (body.get("isActive") != null) plan.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) plan.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        if (body.get("remarks") != null) plan.setRemarks(body.get("remarks").toString());
        return ResponseEntity.ok(planRepository.save(plan));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        planRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Plan deleted"));
    }

    // ---- lookups ----
    @GetMapping("/lookups")
    public ResponseEntity<?> lookups() {
        return ResponseEntity.ok(lookupRepository.findAll());
    }

    @PostMapping("/lookups")
    public ResponseEntity<Lookup> createLookup(@RequestBody Lookup lookup) {
        return ResponseEntity.ok(lookupRepository.save(lookup));
    }

    @PutMapping("/lookups/{id}")
    public ResponseEntity<Lookup> updateLookup(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var lookup = lookupRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) lookup.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) lookup.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        if (body.get("remarks") != null) lookup.setRemarks(body.get("remarks").toString());
        return ResponseEntity.ok(lookupRepository.save(lookup));
    }

    @DeleteMapping("/lookups/{id}")
    public ResponseEntity<Map<String, String>> deleteLookup(@PathVariable Long id) {
        lookupRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Lookup deleted"));
    }

    // ---- goals ----
    @GetMapping("/goals")
    public ResponseEntity<?> goals(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        Page<?> page;
        if (query == null) {
            if (userId != null) page = goalRepository.findByUserId(userId, pageable);
            else page = goalRepository.findAll(pageable);
        } else {
            page = goalRepository.searchAdmin(userId, query, pageable);
        }
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : page.getContent()) {
            var g = (com.officebuddy.goal.Goal) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", g.getId().toString());
            m.put("userId", g.getUserId() != null ? g.getUserId().toString() : null);
            m.put("title", g.getTitle());
            m.put("description", g.getDescription());
            m.put("category", g.getCategory());
            m.put("targetDate", g.getTargetDate() != null ? g.getTargetDate().toString() : null);
            m.put("status", g.getStatus());
            m.put("isActive", g.getIsActive());
            if (g.getUserId() != null) {
                var user = userRepository.findById(g.getUserId()).orElse(null);
                if (user != null) { m.put("userName", user.getName()); m.put("userEmail", user.getEmail()); }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @PutMapping("/goals/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var goal = goalRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) goal.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) goal.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        return ResponseEntity.ok(goalRepository.save(goal));
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable UUID id) {
        goalRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Goal deleted"));
    }

    // ---- todos (tasks & notes) ----
    @GetMapping("/todos")
    public ResponseEntity<?> todos(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        String t = (type == null || type.isBlank()) ? null : type.trim().toLowerCase();
        Page<?> page;
        if (query == null && t == null) {
            if (userId != null) page = todoRepository.findByUserId(userId, pageable);
            else page = todoRepository.findAll(pageable);
        } else {
            page = todoRepository.searchAdmin(userId, t, query, pageable);
        }
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var obj : page.getContent()) {
            var td = (com.officebuddy.todo.Todo) obj;
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", td.getId().toString());
            m.put("userId", td.getUserId() != null ? td.getUserId().toString() : null);
            m.put("title", td.getTitle());
            m.put("content", td.getContent());
            m.put("type", td.getType());
            m.put("dueDate", td.getDueDate() != null ? td.getDueDate().toString() : null);
            m.put("completed", td.isCompleted());
            m.put("isActive", td.getIsActive());
            if (td.getUserId() != null) {
                var user = userRepository.findById(td.getUserId()).orElse(null);
                if (user != null) { m.put("userName", user.getName()); m.put("userEmail", user.getEmail()); }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @PutMapping("/todos/{id}")
    public ResponseEntity<?> updateTodo(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var todo = todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) todo.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) todo.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        return ResponseEntity.ok(todoRepository.save(todo));
    }

    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Map<String, String>> deleteTodo(@PathVariable UUID id) {
        todoRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Todo deleted"));
    }

    // ---- pack downloads ----
    @GetMapping("/pack-downloads")
    public ResponseEntity<?> packDownloads(@PageableDefault(size = 20) Pageable pageable) {
        var page = packDownloadDetailsRepository.findAll(pageable);
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var d : page.getContent()) {
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", d.getId().toString());
            m.put("userId", d.getUserId() != null ? d.getUserId().toString() : null);
            m.put("packId", d.getPackId() != null ? d.getPackId().toString() : null);
            m.put("downloadCount", d.getDownloadCount());
            m.put("active", d.getActive());
            m.put("downloadedAt", d.getDownloadedAt() != null ? d.getDownloadedAt().toString() : null);
            m.put("ipAddress", d.getIpAddress());
            m.put("selectedTypesSnapshot", d.getSelectedTypesSnapshot());
            m.put("createdAt", d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
            if (d.getUserId() != null) {
                var user = userRepository.findById(d.getUserId()).orElse(null);
                if (user != null) {
                    m.put("userName", user.getName());
                    m.put("userEmail", user.getEmail());
                }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/pack-downloads/{id}")
    public ResponseEntity<Map<String, String>> deletePackDownload(@PathVariable UUID id) {
        packDownloadDetailsRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Download record deleted"));
    }

    // ---- reminders ----
    @GetMapping("/reminders")
    public ResponseEntity<?> reminders(@RequestParam(required = false) UUID userId, @PageableDefault(size = 20) Pageable pageable) {
        var page = (userId != null) ? reminderRepository.findByUserId(userId, pageable) : reminderRepository.findAll(pageable);
        var enriched = new java.util.ArrayList<Map<String, Object>>();
        for (var r : page.getContent()) {
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", r.getId().toString());
            m.put("userId", r.getUserId() != null ? r.getUserId().toString() : null);
            m.put("title", r.getTitle());
            m.put("description", r.getDescription());
            m.put("type", r.getType());
            m.put("category", r.getCategory());
            m.put("remindAt", r.getRemindAt() != null ? r.getRemindAt().toString() : null);
            m.put("companyId", r.getCompanyId() != null ? r.getCompanyId().toString() : null);
            m.put("isActive", r.getIsActive());
            if (r.getUserId() != null) {
                var user = userRepository.findById(r.getUserId()).orElse(null);
                if (user != null) { m.put("userName", user.getName()); m.put("userEmail", user.getEmail()); }
            }
            enriched.add(m);
        }
        var out = new java.util.LinkedHashMap<String, Object>();
        out.put("content", enriched);
        out.put("totalElements", page.getTotalElements());
        out.put("totalPages", page.getTotalPages());
        out.put("number", page.getNumber());
        return ResponseEntity.ok(out);
    }

    @PutMapping("/reminders/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        var reminder = reminderRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) reminder.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) reminder.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        return ResponseEntity.ok(reminderRepository.save(reminder));
    }

    @DeleteMapping("/reminders/{id}")
    public ResponseEntity<Map<String, String>> deleteReminder(@PathVariable UUID id) {
        reminderRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Reminder deleted"));
    }

    // ---- ad providers ----
    @GetMapping("/ad-providers")
    public ResponseEntity<?> adProviders() {
        return ResponseEntity.ok(adProviderRepository.findAll());
    }

    @PostMapping("/ad-providers")
    public ResponseEntity<AdProvider> createAdProvider(@RequestBody AdProvider provider) {
        provider.setId(null);
        return ResponseEntity.ok(adProviderRepository.save(provider));
    }

    @PutMapping("/ad-providers/{id}")
    public ResponseEntity<AdProvider> updateAdProvider(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var provider = adProviderRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("providerName") != null) provider.setProviderName(body.get("providerName").toString());
        if (body.get("platform") != null) provider.setPlatform(body.get("platform").toString());
        if (body.get("priority") != null) provider.setPriority(Integer.parseInt(body.get("priority").toString()));
        if (body.get("isActive") != null) provider.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        return ResponseEntity.ok(adProviderRepository.save(provider));
    }

    @DeleteMapping("/ad-providers/{id}")
    public ResponseEntity<Map<String, String>> deleteAdProvider(@PathVariable Long id) {
        adProviderRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Provider deleted"));
    }

    // ---- ad configs ----
    @GetMapping("/ad-configs")
    public ResponseEntity<?> adConfigs() {
        return ResponseEntity.ok(adConfigRepository.findAll());
    }

    @PostMapping("/ad-configs")
    public ResponseEntity<AdConfig> createAdConfig(@RequestBody AdConfig config) {
        config.setId(null);
        return ResponseEntity.ok(adConfigRepository.save(config));
    }

    @PutMapping("/ad-configs/{id}")
    public ResponseEntity<AdConfig> updateAdConfig(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var config = adConfigRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("isActive") != null) config.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("priority") != null) config.setPriority(Integer.parseInt(body.get("priority").toString()));
        return ResponseEntity.ok(adConfigRepository.save(config));
    }

    @DeleteMapping("/ad-configs/{id}")
    public ResponseEntity<Map<String, String>> deleteAdConfig(@PathVariable Long id) {
        adConfigRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Ad config deleted"));
    }

    // ---- custom ads ----
    @GetMapping("/custom-ads")
    public ResponseEntity<?> customAds() {
        return ResponseEntity.ok(customAdRepository.findAll());
    }

    @PostMapping("/custom-ads")
    public ResponseEntity<CustomAd> createCustomAd(@RequestBody CustomAd ad) {
        ad.setId(null);
        return ResponseEntity.ok(customAdRepository.save(ad));
    }

    @PutMapping("/custom-ads/{id}")
    public ResponseEntity<CustomAd> updateCustomAd(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var ad = customAdRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("title") != null) ad.setTitle(body.get("title").toString());
        if (body.get("productImgLink") != null) ad.setProductImgLink(body.get("productImgLink").toString());
        if (body.get("productOpenLink") != null) ad.setProductOpenLink(body.get("productOpenLink").toString());
        if (body.get("isActive") != null) ad.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("isDeleted") != null) ad.setIsDeleted(Integer.parseInt(body.get("isDeleted").toString()));
        if (body.get("remarks") != null) ad.setRemarks(body.get("remarks").toString());
        return ResponseEntity.ok(customAdRepository.save(ad));
    }

    @DeleteMapping("/custom-ads/{id}")
    public ResponseEntity<Map<String, String>> deleteCustomAd(@PathVariable Long id) {
        customAdRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Custom ad deleted"));
    }

    // ---- security settings ----
    @GetMapping("/security-settings")
    public ResponseEntity<?> securitySettings() {
        return ResponseEntity.ok(securitySettingRepository.findAll());
    }

    @PutMapping("/security-settings/{id}")
    public ResponseEntity<SecuritySetting> updateSecuritySetting(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var setting = securitySettingRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("configValue") != null) setting.setConfigValue(body.get("configValue").toString());
        if (body.get("isActive") != null) setting.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        if (body.get("description") != null) setting.setDescription(body.get("description").toString());
        return ResponseEntity.ok(securitySettingRepository.save(setting));
    }

    // ---- payment configs ----
    @GetMapping("/payment-configs")
    public ResponseEntity<?> paymentConfigs() {
        return ResponseEntity.ok(paymentConfigRepository.findAll());
    }

    @PutMapping("/payment-configs/{id}")
    public ResponseEntity<PaymentConfig> updatePaymentConfig(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var config = paymentConfigRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (body.get("keyId") != null) config.setKeyId(body.get("keyId").toString());
        if (body.get("secretKey") != null) config.setSecretKey(body.get("secretKey").toString());
        if (body.get("isActive") != null) config.setIsActive(Integer.parseInt(body.get("isActive").toString()));
        return ResponseEntity.ok(paymentConfigRepository.save(config));
    }

    @DeleteMapping("/payment-configs/{id}")
    public ResponseEntity<Map<String, String>> deletePaymentConfig(@PathVariable Long id) {
        paymentConfigRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Payment config deleted"));
    }
}
