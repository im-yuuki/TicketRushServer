import type { ReactNode } from "react";

import { emailTheme } from "../theme/tokens";

type EmailShellProps = {
  title: string;
  previewText: string;
  darkModeCss: string;
  children: ReactNode;
};

export function EmailShell({ title, previewText, darkModeCss, children }: EmailShellProps) {
  return (
    <html lang="en">
      <head>
        <meta charSet="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <meta name="color-scheme" content="light dark" />
        <meta name="supported-color-schemes" content="light dark" />
        <title>{title}</title>
        <style>{darkModeCss}</style>
      </head>
      <body
        data-email-root="true"
        className="email-shell"
        style={{
          margin: 0,
          padding: 0,
          backgroundColor: emailTheme.color.light.background,
          color: emailTheme.color.light.foreground,
          fontFamily: emailTheme.font.body,
          WebkitTextSizeAdjust: "100%",
        }}
      >
        <div
          style={{
            display: "none",
            maxHeight: 0,
            maxWidth: 0,
            overflow: "hidden",
            opacity: 0,
          }}
        >
          {previewText}
        </div>

        <table role="presentation" width="100%" cellPadding="0" cellSpacing="0" style={{ width: "100%" }}>
          <tbody>
            <tr>
              <td align="center" style={{ padding: "32px 16px" }}>
                <table
                  role="presentation"
                  width="100%"
                  cellPadding="0"
                  cellSpacing="0"
                  className="email-card"
                  style={{
                    width: "100%",
                    maxWidth: "640px",
                    backgroundColor: emailTheme.color.light.card,
                    border: `1px solid ${emailTheme.color.light.border}`,
                    borderRadius: `${emailTheme.radius.shell}px`,
                    boxShadow: `0 18px 48px ${emailTheme.color.light.shadow}`,
                  }}
                >
                  <tbody>
                    <tr>
                      <td style={{ padding: "32px 28px" }}>{children}</td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
      </body>
    </html>
  );
}
