import { BrandHeader } from "../components/BrandHeader";
import { EmailShell } from "../components/EmailShell";
import { OtpCard } from "../components/OtpCard";
import { emailTheme } from "../theme/tokens";

type PasswordResetEmailProps = {
  darkModeCss: string;
  userName: string;
  otpCode: string;
};

export function PasswordResetEmail({ darkModeCss, userName, otpCode }: PasswordResetEmailProps) {
  return (
    <EmailShell
      title="Reset your TicketRush password"
      previewText="Reset your TicketRush password with your one-time passcode."
      darkModeCss={darkModeCss}
    >
      <BrandHeader
        badge="Password reset"
        title="Reset your password securely"
        body="Use the one-time passcode below to keep your TicketRush account moving without sharing a permanent password reset link."
      />

      <table role="presentation" width="100%" cellPadding="0" cellSpacing="0" style={{ width: "100%", marginTop: "28px" }}>
        <tbody>
          <tr>
            <td>
              <p
                className="email-text"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "16px",
                  lineHeight: 1.7,
                }}
              >
                Hi <span data-th-text="${userName}">{userName}</span>,
              </p>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "14px" }}>
              <OtpCard
                label="Your reset code"
                code={<span data-th-text="${otpCode}">{otpCode}</span>}
                hint="Enter this passcode in the app to continue resetting your password. If you did not request this, you can ignore this email and keep your current password."
              />
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "18px" }}>
              <p
                className="meta-text"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "14px",
                  lineHeight: 1.7,
                }}
              >
                For security, only use the latest code you requested.
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </EmailShell>
  );
}
