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
        title="Confirm your TicketRush account"
        body="Use this code to verify your email address and finish setting up your account."
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
                  fontSize: "15px",
                  lineHeight: 1.6,
                }}
              >
                Hi <span data-th-text="${userName}">{userName}</span>,
              </p>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "14px" }}>
              <OtpCard
                code={<span data-th-text="${otpCode}">{otpCode}</span>}
                hint="Enter this code in TicketRush to confirm your email address."
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
                  fontSize: "13px",
                  lineHeight: 1.6,
                }}
              >
                This code only confirms this email address. If you did not create a TicketRush account, ignore this message. Need help? Reply to this email and the TicketRush team can help you finish setup.
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </EmailShell>
  );
}
