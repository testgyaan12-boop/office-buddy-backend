package com.officebuddy.reminder.controller;

import com.officebuddy.reminder.dto.ReminderHistoryDto;
import com.officebuddy.reminder.dto.ReminderRequest;
import com.officebuddy.reminder.dto.ReminderResponse;
import com.officebuddy.reminder.service.ReminderService;
import com.officebuddy.storage.StorageService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final StorageService storageService;

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> list(Authentication auth, @RequestParam(required = false) String category) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.getReminders(user.getId(), category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> get(Authentication auth, @PathVariable UUID id) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.getReminder(user.getId(), id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReminderResponse> createMultipart(
            Authentication auth,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "jd", required = false) String jd,
            @RequestParam("type") String type,
            @RequestParam("category") String category,
            @RequestParam(value = "companyId", required = false) String companyId,
            @RequestParam("remindAt") String remindAt,
            @RequestParam(value = "notifyBeforeMinutes", required = false) Integer notifyBeforeMinutes,
            @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        var user = (User) auth.getPrincipal();
        var req = new ReminderRequest();
        req.setTitle(title);
        req.setDescription(description);
        req.setJd(jd);
        req.setType(type);
        req.setCategory(category);
        req.setCompanyId(companyId);
        req.setRemindAt(remindAt);
        req.setNotifyBeforeMinutes(notifyBeforeMinutes);
        req.setDeviceId(deviceId);
        if (file != null && !file.isEmpty()) {
            var result = storageService.uploadFile(file);
            req.setFileKey(result.getKey());
            req.setFileUrl(result.getUrl());
            req.setFileName(file.getOriginalFilename());
        }
        return ResponseEntity.ok(reminderService.createReminder(user.getId(), req));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReminderResponse> create(Authentication auth, @RequestBody ReminderRequest req) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.createReminder(user.getId(), req));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        var result = storageService.uploadFile(file);
        Map<String, String> res = new HashMap<>();
        res.put("fileKey", result.getKey());
        res.put("fileUrl", result.getUrl());
        res.put("fileName", file.getOriginalFilename());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> update(Authentication auth, @PathVariable UUID id, @RequestBody ReminderRequest req) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.updateReminder(user.getId(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable UUID id) {
        var user = (User) auth.getPrincipal();
        reminderService.deleteReminder(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/history")
    public ResponseEntity<ReminderHistoryDto> logHistory(Authentication auth, @PathVariable UUID id, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        String deviceId = body != null ? body.get("deviceId") : null;
        String status = body != null ? body.get("status") : "SENT";
        return ResponseEntity.ok(reminderService.logNotification(user.getId(), id, deviceId, status));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReminderHistoryDto>> historyAll(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.getHistory(user.getId(), null));
    }

    @GetMapping("/history/{reminderId}")
    public ResponseEntity<List<ReminderHistoryDto>> historyFor(Authentication auth, @PathVariable UUID reminderId) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(reminderService.getHistory(user.getId(), reminderId));
    }
}
