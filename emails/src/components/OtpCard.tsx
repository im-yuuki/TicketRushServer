import type { ReactNode } from "react";

import { emailTheme } from "../theme/tokens";

type OtpCardProps = {
  code: ReactNode;
  hint: string;
};

export function OtpCard({ code, hint }: OtpCardProps) {
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
          <td style={{ padding: "18px" }}>
            <div
              className="accent-surface"
              style={{
                padding: "14px 16px",
                borderRadius: `${emailTheme.radius.otp}px`,
                border: `1px solid ${emailTheme.color.light.outline}`,
                backgroundColor: emailTheme.color.light.otpBackground,
              }}
            >
              <div
                className="otp-code"
                style={{
                  color: emailTheme.color.light.otpForeground,
                  fontFamily: emailTheme.font.code,
                  fontSize: "24px",
                  fontWeight: 600,
                  letterSpacing: "0.18em",
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
                fontSize: "13px",
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
