// lhci-login.js
// Script to log into a webapp before running Lighthouse scans.
// Usage: LHCI calls this before collecting each URL.
// Reads creds and base from env to avoid hardcoding.
const BASE = process.env.A11Y_BASE || "http://localhost:7001";
const USER = process.env.A11Y_USER || "app";
const PASS = process.env.A11Y_PASS || "app_pass";

module.exports = async ({ page, url }) => {
  await page.goto(`${BASE}/login`, {
    waitUntil: "networkidle2",
    timeout: 120000,
  });

  // Try common names/ids first and fall back to best effort queries.
  const userSelCandidates = [
    "#username",
    'input[name="username"]',
    'input[type="text"]',
  ];
  const passSelCandidates = [
    "#password",
    'input[name="password"]',
    'input[type="password"]',
  ];
  const submitSelCandidates = [
    'button[type="submit"]',
    'input[type="submit"]',
    "button#login",
    'button[name="login"]',
  ];

  async function findFirst(selList) {
    for (const s of selList) {
      const el = await page.$(s);
      if (el) return s;
    }
    return null;
  }

  const userSel = await findFirst(userSelCandidates);
  const passSel = await findFirst(passSelCandidates);
  const submitSel = await findFirst(submitSelCandidates);

  if (!userSel || !passSel || !submitSel) {
    throw new Error(
      `Login selectors not found. Found -> user:${!!userSel} pass:${!!passSel} submit:${!!submitSel}`
    );
  }

  await page.click(userSel, { clickCount: 3 });
  await page.type(userSel, USER, { delay: 10 });
  await page.click(passSel, { clickCount: 3 });
  await page.type(passSel, PASS, { delay: 10 });

  await Promise.all([
    page.click(submitSel),
    page.waitForNavigation({ waitUntil: "networkidle2", timeout: 120000 }),
  ]);

  // Make sure it logged in
  const cur = page.url();
  if (/\/login(\b|\/|$)/i.test(cur)) {
    throw new Error(`Still on login page after submit -> ${cur}`);
  }

  // Move onto the target URL
  await page.goto(url, { waitUntil: "networkidle2", timeout: 120000 });
};
