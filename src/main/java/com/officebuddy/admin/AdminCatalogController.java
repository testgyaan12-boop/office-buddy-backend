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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    // ---- documents ----
    @GetMapping("/documents")
    public ResponseEntity<?> documents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        if (query == null) {
            if (userId != null) return ResponseEntity.ok(documentRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(documentRepository.findAll(pageable));
        }
        return ResponseEntity.ok(documentRepository.searchAdmin(userId, query, pageable));
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
        if (query == null && f == null && t == null) {
            if (userId != null) return ResponseEntity.ok(companyRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(companyRepository.findAll(pageable));
        }
        return ResponseEntity.ok(companyRepository.searchAdmin(userId, query, f, t, pageable));
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
        if (query == null) {
            if (userId != null) return ResponseEntity.ok(subscriptionRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(subscriptionRepository.findAll(pageable));
        }
        return ResponseEntity.ok(subscriptionRepository.searchAdmin(userId, query, pageable));
    }

    @GetMapping("/subscriptions/{id}")
    public ResponseEntity<Subscription> subscription(@PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found")));
    }

    // ---- invoices ----
    @GetMapping("/invoices")
    public ResponseEntity<?> invoices(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        if (query == null) {
            if (userId != null) return ResponseEntity.ok(invoiceRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(invoiceRepository.findAll(pageable));
        }
        return ResponseEntity.ok(invoiceRepository.searchAdmin(userId, query, pageable));
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
        if (query == null) {
            if (userId != null) return ResponseEntity.ok(goalRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(goalRepository.findAll(pageable));
        }
        return ResponseEntity.ok(goalRepository.searchAdmin(userId, query, pageable));
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
        if (query == null && t == null) {
            if (userId != null) return ResponseEntity.ok(todoRepository.findByUserId(userId, pageable));
            return ResponseEntity.ok(todoRepository.findAll(pageable));
        }
        return ResponseEntity.ok(todoRepository.searchAdmin(userId, t, query, pageable));
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
        return ResponseEntity.ok(packDownloadDetailsRepository.findAll(pageable));
    }

    @DeleteMapping("/pack-downloads/{id}")
    public ResponseEntity<Map<String, String>> deletePackDownload(@PathVariable UUID id) {
        packDownloadDetailsRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Download record deleted"));
    }

    // ---- reminders ----
    @GetMapping("/reminders")
    public ResponseEntity<?> reminders(@RequestParam(required = false) UUID userId, @PageableDefault(size = 20) Pageable pageable) {
        if (userId != null) return ResponseEntity.ok(reminderRepository.findByUserId(userId, pageable));
        return ResponseEntity.ok(reminderRepository.findAll(pageable));
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
