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
      <BrandHeader title="Reset your TicketRush password" />

      <table role="presentation" width="100%" cellPadding="0" cellSpacing="0" style={{ width: "100%", marginTop: "28px" }}>
        <tbody>
          <tr>
            <td>
              <p
                className="email-text"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "15px",
                  lineHeight: 1.6,
                }}
              >
                Hi <span data-th-text="${userName}">{userName}</span>,
              </p>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "12px" }}>
              <p
                className="email-muted"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.muted,
                  fontSize: "15px",
                  lineHeight: 1.6,
                }}
              >
                Use this code to continue resetting your password. Keep it private and enter it only in TicketRush.
              </p>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "14px" }}>
              <OtpCard code={<span data-th-text="${otpCode}">{otpCode}</span>} />
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "18px" }}>
              <p
                className="meta-text"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "13px",
                  lineHeight: 1.6,
                }}
              >
                Do not share it with anyone, including TicketRush staff. If you did not request it, ignore this message and keep your current password.
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </EmailShell>
  );
}
