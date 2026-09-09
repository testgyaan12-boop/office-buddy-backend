package com.officebuddy.auth;

import com.officebuddy.auth.dto.VerifyEmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verify-email")
@RequiredArgsConstructor
public class VerifyEmailPageController {

    private final AuthService authService;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verify(@RequestParam(value = "token", required = false) String token) {
        boolean ok;
        String message;
        if (token == null || token.isBlank()) {
            ok = false;
            message = "Missing verification token.";
        } else {
            try {
                var req = new VerifyEmailRequest();
                req.setToken(token);
                var result = authService.verifyEmail(req);
                ok = true;
                message = result.getOrDefault("message", "Email verified successfully. You can now log in.");
            } catch (Exception e) {
                ok = false;
                message = e.getMessage() != null ? e.getMessage() : "Verification failed.";
            }
        }
        String safe = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String color = ok ? "#16a34a" : "#dc2626";
        String title = ok ? "Email verified" : "Verification failed";
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 40px; background: #f4f4f4;">
                <div style="max-width: 480px; margin: auto; background: white; border-radius: 12px; padding: 32px; text-align: center;">
                    <h2 style="color: %s;">%s</h2>
                    <p style="color: #666;">%s</p>
                    <p style="color: #999; font-size: 13px;">You can now log in to OfficeBuddy.</p>
                </div>
            </body>
            </html>
            """.formatted(color, title, safe);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
