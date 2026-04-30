import { emailTheme } from "../theme/tokens";

type BrandHeaderProps = {
  badge: string;
  title: string;
  body: string;
};

export function BrandHeader({ badge, title, body }: BrandHeaderProps) {
  return (
    <>
      <table role="presentation" width="100%" cellPadding="0" cellSpacing="0" style={{ width: "100%" }}>
        <tbody>
          <tr>
            <td>
              <table role="presentation" cellPadding="0" cellSpacing="0">
                <tbody>
                  <tr>
                    <td className="brand-primary" style={wordmarkStyle}>
                      ticket
                      <span className="brand-accent">rush</span>
                    </td>
                    <td style={{ paddingLeft: "8px" }}>
                      <BrandSquare />
                    </td>
                  </tr>
                </tbody>
              </table>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "20px" }}>
              <span
                className="accent-pill meta-text"
                style={{
                  display: "inline-block",
                  padding: "8px 12px",
                  borderRadius: `${emailTheme.radius.badge}px`,
                  border: `1px solid ${emailTheme.color.light.outline}`,
                  backgroundColor: emailTheme.color.light.pillBackground,
                  color: emailTheme.color.light.accentForeground,
                  fontSize: "12px",
                  fontWeight: 700,
                  letterSpacing: "0.08em",
                  textTransform: "uppercase",
                }}
              >
                {badge}
              </span>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "20px" }}>
              <h1
                className="email-title"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "30px",
                  lineHeight: 1.2,
                  fontWeight: 800,
                }}
              >
                {title}
              </h1>
            </td>
          </tr>
          <tr>
            <td style={{ paddingTop: "12px" }}>
              <p
                className="email-muted"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.muted,
                  fontSize: "16px",
                  lineHeight: 1.7,
                }}
              >
                {body}
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </>
  );
}

function BrandSquare() {
  return (
    <svg
      width="28"
      height="28"
      viewBox="0 0 24 24"
      aria-hidden="true"
      style={{ display: "block", color: emailTheme.color.light.accent }}
    >
      <path
        fill="currentColor"
        d="M5.055 7.06C3.805 6.347 2.25 7.25 2.25 8.69v8.122c0 1.44 1.555 2.343 2.805 1.628L12 14.471v2.34c0 1.44 1.555 2.343 2.805 1.628l7.108-4.061c1.26-.72 1.26-2.536 0-3.256l-7.108-4.061C13.555 6.346 12 7.249 12 8.689v2.34L5.055 7.061Z"
      />
    </svg>
  );
}

const wordmarkStyle = {
  color: emailTheme.color.light.foreground,
  fontFamily: emailTheme.font.brand,
  fontSize: "30px",
  fontWeight: 900,
  lineHeight: 1,
  letterSpacing: "-0.05em",
} as const;
