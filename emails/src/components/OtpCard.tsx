import { emailTheme } from "../theme/tokens";

type OtpCardProps = {
  label: string;
  code: string;
  hint: string;
};

export function OtpCard({ label, code, hint }: OtpCardProps) {
  return (
    <table
      role="presentation"
      width="100%"
      cellPadding="0"
      cellSpacing="0"
      className="email-card-secondary"
      style={{
        width: "100%",
        backgroundColor: emailTheme.color.light.cardSecondary,
        border: `1px solid ${emailTheme.color.light.border}`,
        borderRadius: `${emailTheme.radius.card}px`,
      }}
    >
      <tbody>
        <tr>
          <td style={{ padding: "20px" }}>
            <p
              className="otp-caption email-caption"
              style={{
                margin: 0,
                color: emailTheme.color.light.muted,
                fontSize: "12px",
                fontWeight: 700,
                letterSpacing: "0.08em",
                textTransform: "uppercase",
              }}
            >
              {label}
            </p>

            <div
              className="accent-surface"
              style={{
                marginTop: "14px",
                padding: "18px 20px",
                borderRadius: `${emailTheme.radius.otp}px`,
                border: `1px solid ${emailTheme.color.light.outline}`,
                backgroundColor: emailTheme.color.light.otpBackground,
              }}
            >
              <div
                className="otp-code"
                style={{
                  color: emailTheme.color.light.otpForeground,
                  fontSize: "28px",
                  fontWeight: 900,
                  letterSpacing: "0.24em",
                  textAlign: "center",
                }}
              >
                {code}
              </div>
            </div>

            <p
              className="meta-muted"
              style={{
                margin: "14px 0 0",
                color: emailTheme.color.light.muted,
                fontSize: "14px",
                lineHeight: 1.6,
              }}
            >
              {hint}
            </p>
          </td>
        </tr>
      </tbody>
    </table>
  );
}
