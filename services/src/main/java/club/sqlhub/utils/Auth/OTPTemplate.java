package club.sqlhub.utils.Auth;

public class OTPTemplate {
    public static String getOtpHtmlTemplate(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; padding: 20px; background: #f9f9f9;">
                    <div style="max-width: 500px; margin: auto; background: #ffffff; padding: 20px; border-radius: 10px; border: 1px solid #eee;">

                        <h2 style="text-align: center; color: #4CAF50; margin-top: 0;">Your OTP Code</h2>

                        <p style="font-size: 16px; color: #333;">
                            Hello,
                        </p>

                        <p style="font-size: 16px; color: #333;">
                            Your one-time password (OTP) for verification is:
                        </p>

                        <div style="text-align: center; margin: 25px 0;">
                            <div style="display: inline-block;
                                        font-size: 32px;
                                        letter-spacing: 10px;
                                        padding: 15px 25px;
                                        border-radius: 8px;
                                        background: #4CAF50;
                                        color: white;">
                                """
                + otp + """
                                    </div>
                                </div>

                                <p style="font-size: 16px; color: #333;">
                                    This OTP is valid for <b>5 minutes</b>. Do not share it with anyone.
                                </p>

                                <p style="font-size: 14px; color: #777; margin-top: 30px;">
                                    If you did not request this OTP, please ignore this email or contact support.
                                </p>

                                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">

                                <p style="font-size: 13px; color: #777; text-align: center;">
                                    © 2025 SQLHub • Secure Login System
                                </p>

                            </div>
                        </div>
                        """;
    }

}
