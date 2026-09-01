package com.officebuddy.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate;
    private final String resendApiKey;
    private final String fromAddress;

    public EmailService(RestTemplate restTemplate,
                        @Value("${app.email.resend-api-key:}") String resendApiKey,
                        @Value("${app.email.from-address:OfficeBuddy <noreply@packsyourbags.in>}") String fromAddress) {
        this.restTemplate = restTemplate;
        this.resendApiKey = resendApiKey;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(String to, String name, String token) {
        String subject = "Verify your OfficeBuddy account";
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 40px; background: #f4f4f4;">
                <div style="max-width: 480px; margin: auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display: inline-block; background: linear-gradient(135deg, #7C3AED, #3B82F6); width: 48px; height: 48px; border-radius: 12px; line-height: 48px; color: white; font-size: 20px; font-weight: bold;">OB</div>
                    </div>
                    <h2 style="color: #333; text-align: center;">Verify your email</h2>
                    <p style="color: #666; line-height: 1.6;">Hi %s,</p>
                    <p style="color: #666; line-height: 1.6;">Thanks for signing up! Please verify your email address by clicking the button below:</p>
                    <div style="text-align: center; margin: 28px 0;">
                        <a href="https://officebuddy.app/verify-email?token=%s"
                           style="display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #7C3AED, #3B82F6); color: white; text-decoration: none; border-radius: 8px; font-weight: 600;">
                            Verify Email
                        </a>
                    </div>
                    <p style="color: #999; font-size: 13px; text-align: center;">Or paste this token in the app:</p>
                    <div style="text-align: center; margin: 12px 0; padding: 12px; background: #f8f8f8; border-radius: 8px; font-family: monospace; font-size: 18px; letter-spacing: 2px; color: #333;">%s</div>
                    <p style="color: #999; font-size: 12px; text-align: center; margin-top: 24px;">If you didn't create this account, please ignore this email.</p>
                </div>
            </body>
            </html>
            """.formatted(name, token, token);

        sendEmail(to, subject, html);
    }

    public void sendResetOtp(String to, String name, String otp) {
        String subject = "Reset your OfficeBuddy password";
        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 40px; background: #f4f4f4;">
                <div style="max-width: 480px; margin: auto; background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.08);">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <div style="display: inline-block; background: linear-gradient(135deg, #7C3AED, #3B82F6); width: 48px; height: 48px; border-radius: 12px; line-height: 48px; color: white; font-size: 20px; font-weight: bold;">OB</div>
                    </div>
                    <h2 style="color: #333; text-align: center;">Password Reset OTP</h2>
                    <p style="color: #666; line-height: 1.6;">Hi %s,</p>
                    <p style="color: #666; line-height: 1.6;">We received a request to reset your password. Use the following OTP:</p>
                    <div style="text-align: center; margin: 28px 0; padding: 16px; background: #f0f4ff; border-radius: 12px; border: 2px dashed #7C3AED;">
                        <span style="font-family: monospace; font-size: 36px; letter-spacing: 8px; color: #7C3AED; font-weight: bold;">%s</span>
                    </div>
                    <p style="color: #999; font-size: 13px; text-align: center;">This OTP expires in 15 minutes.</p>
                    <p style="color: #999; font-size: 12px; text-align: center; margin-top: 24px;">If you didn't request this, please ignore this email.</p>
                </div>
            </body>
            </html>
            """.formatted(name, otp);

        sendEmail(to, subject, html);
    }

    private void sendEmail(String to, String subject, String html) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(resendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var body = Map.of(
            "from", fromAddress,
            "to", new String[]{to},
            "subject", subject,
            "html", html
        );

        var request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(RESEND_API_URL, request, String.class);
    }
}
