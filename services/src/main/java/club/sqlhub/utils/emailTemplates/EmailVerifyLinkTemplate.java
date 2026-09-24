package club.sqlhub.utils.emailTemplates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailVerifyLinkTemplate {

    @Value("${app.domain}")
    private String appDomain;

    public String getVerifyLinkTemplate(String token) {
        String verifyLink = appDomain + "/verify-email?token=" + token;

        return """
                <div style="font-family: Arial, sans-serif; padding: 20px; background: #f9f9f9;">
                  <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 24px; border-radius: 10px; border: 1px solid #eee;">
                    <h2 style="text-align: center; color: #333; margin-top: 0;">Verify your email</h2>

                    <p style="font-size: 16px; color: #333;">
                      Hi,
                    </p>

                    <p style="font-size: 16px; color: #333;">
                      Click the button below to verify your email address and complete your registration.
                    </p>

                    <div style="text-align:center; margin: 28px 0;">
                      <a href="%s"
                         target="_blank"
                         rel="noopener noreferrer"
                         style="display: inline-block; text-decoration: none; font-size: 16px; padding: 14px 28px; border-radius: 8px; background: #4CAF50; color: #ffffff; font-weight: 600;">
                        Verify Email
                      </a>
                    </div>

                    <p style="font-size: 14px; color: #555;">
                      If the button above does not work, copy and paste the following link into your browser:
                    </p>

                    <p style="word-break: break-all; font-size: 14px; color: #4CAF50;">
                      <a href="%s" target="_blank" rel="noopener noreferrer">%s</a>
                    </p>

                    <p style="font-size: 14px; color: #555;">
                      This link is valid for <b>15 minutes</b>.
                    </p>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 22px 0;">

                    <p style="font-size: 13px; color: #777; text-align: center;">
                      If you did not request this, you can safely ignore this email.
                    </p>

                    <p style="font-size: 13px; color: #777; text-align: center; margin-top: 8px;">
                      © 2025 SQLHub • Secure Login System
                    </p>
                  </div>
                </div>
                """.formatted(verifyLink, verifyLink, verifyLink);
    }
}
