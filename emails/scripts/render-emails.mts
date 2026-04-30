import { mkdir, readdir, readFile, rm, writeFile } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

import juice from "juice";
import { createElement, type ReactElement } from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { build as viteBuild } from "vite";

import { PasswordResetEmail } from "../src/templates/PasswordResetEmail";
import { RegisterConfirmationEmail } from "../src/templates/RegisterConfirmationEmail";

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, "..");
const distDir = join(rootDir, "dist");
const renderedDir = join(distDir, "rendered");
const darkModeCssPath = join(rootDir, "src", "theme", "dark-mode.css");

const placeholderUserName = "__USER_NAME__";
const placeholderOtpCode = "__OTP_CODE__";

async function main() {
  await viteBuild({
    configFile: join(rootDir, "vite.config.ts"),
    logLevel: "error",
  });

  const [baseCss, darkModeCss] = await Promise.all([
    readCompiledCss(),
    readFile(darkModeCssPath, "utf8"),
  ]);

  await rm(renderedDir, { recursive: true, force: true });
  await mkdir(renderedDir, { recursive: true });

  const files = [
    {
      fileName: "register-confirmation-email.html",
      html: renderDocument(
        createElement(RegisterConfirmationEmail, {
          darkModeCss,
          userName: placeholderUserName,
          otpCode: placeholderOtpCode,
        }),
      ),
    },
    {
      fileName: "password-reset-email.html",
      html: renderDocument(
        createElement(PasswordResetEmail, {
          darkModeCss,
          userName: placeholderUserName,
          otpCode: placeholderOtpCode,
        }),
      ),
    },
  ];

  await Promise.all(
    files.map(async ({ fileName, html }) => {
      const outputPath = join(renderedDir, fileName);
      const finalHtml = juice.inlineContent(html, baseCss);
      await writeFile(outputPath, finalHtml, "utf8");
    }),
  );

  console.log(`Rendered ${files.length} email templates to ${renderedDir}`);
}

function renderDocument(markup: ReactElement) {
  return `<!DOCTYPE html>${renderToStaticMarkup(markup)}`;
}

async function readCompiledCss() {
  const entries = await readdir(distDir, { withFileTypes: true });
  const cssFile = entries.find((entry) => entry.isFile() && entry.name.endsWith(".css"));

  if (!cssFile) {
    throw new Error(`No compiled CSS asset found in ${distDir}`);
  }

  return readFile(join(distDir, cssFile.name), "utf8");
}

await main();
