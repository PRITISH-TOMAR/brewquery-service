package club.sqlhub.utils.emailTemplates;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTemplate {

  @Value("${app.domain}")
  private String appDomain;

  public String getPasswordResetTemplate(String resetKey) {
    String resetLink =  appDomain + "/credentials/" + resetKey;

    return """
        <div style="font-family: Arial, sans-serif; padding: 20px; background: #f9f9f9;">
          <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 24px; border-radius: 10px; border: 1px solid #eee;">
            <h2 style="text-align: center; color: #333; margin-top: 0;">Reset your password</h2>

            <p style="font-size: 16px; color: #333;">
              Hi,
            </p>

            <p style="font-size: 16px; color: #333;">
              We received a request to reset your password. Click the button below to reset it now.
            </p>

            <div style="text-align:center; margin: 28px 0;">
              <a href="%s"
                 target="_blank"
                 rel="noopener noreferrer"
                 style="display: inline-block; text-decoration: none; font-size: 16px; padding: 14px 28px; border-radius: 8px; background: #1f8ef1; color: #ffffff; font-weight: 600;">
                Reset Password
              </a>
            </div>

            <p style="font-size: 14px; color: #555;">
              If the button above does not work, copy and paste the following link into your browser:
            </p>

            <p style="word-break: break-all; font-size: 14px; color: #1f8ef1;">
              <a href="%s" target="_blank" rel="noopener noreferrer">%s</a>
            </p>

            <hr style="border: none; border-top: 1px solid #eee; margin: 22px 0;">

            <p style="font-size: 13px; color: #777; text-align: center;">
              If you did not request a password reset, you can safely ignore this email.
            </p>

            <p style="font-size: 13px; color: #777; text-align: center; margin-top: 8px;">
              © 2025 SQLHub • Secure Login System
            </p>
          </div>
        </div>
        """
        .formatted(resetLink, resetLink, resetLink);
  }

}
