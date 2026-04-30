import { emailTheme } from "../theme/tokens";

type BrandHeaderProps = {
  title: string;
};

export function BrandHeader({ title }: BrandHeaderProps) {
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
            <td style={{ paddingTop: "24px" }}>
              <h1
                className="email-title"
                style={{
                  margin: 0,
                  color: emailTheme.color.light.foreground,
                  fontSize: "24px",
                  lineHeight: 1.3,
                  fontWeight: 500,
                }}
              >
                {title}
              </h1>
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
      width="24"
      height="24"
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
  fontSize: "26px",
  fontWeight: 900,
  lineHeight: 1,
  letterSpacing: "-0.05em",
} as const;
