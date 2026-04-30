import type { ReactNode } from "react";

import { emailTheme } from "../theme/tokens";

type OtpCardProps = {
  code: ReactNode;
};

export function OtpCard({ code }: OtpCardProps) {
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
          </td>
        </tr>
      </tbody>
    </table>
  );
}
