import { BrandHeader } from "../components/BrandHeader";
import { EmailShell } from "../components/EmailShell";
import { OtpCard } from "../components/OtpCard";
import { emailTheme } from "../theme/tokens";

type RegisterConfirmationEmailProps = {
  darkModeCss: string;
  userName: string;
  otpCode: string;
};

export function RegisterConfirmationEmail({ darkModeCss, userName, otpCode }: RegisterConfirmationEmailProps) {
  return (
    <EmailShell
      title="Confirm your TicketRush account"
      previewText="Confirm your TicketRush account with your one-time passcode."
      darkModeCss={darkModeCss}
    >
      <BrandHeader
        badge="Email confirmation"
        title="Finish creating your TicketRush account"
        body="Confirm your email address to activate your account and lock in a smoother checkout next time you grab tickets."
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
                label="Your confirmation code"
                code={<span data-th-text="${otpCode}">{otpCode}</span>}
                hint="Enter this passcode in the app to finish registration. If this was not you, you can safely ignore this email."
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
                Need help? Reply to this email and the TicketRush team can help you finish setup.
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </EmailShell>
  );
}
