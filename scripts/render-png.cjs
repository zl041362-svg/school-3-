const puppeteer = require('puppeteer-core');
const path = require('path');

(async () => {
  const root = path.resolve(__dirname, '..');
  const htmlPath = path.join(root, 'docs', 'system-architecture.html');
  const pngPath = path.join(root, 'docs', 'system-architecture.png');

  const browser = await puppeteer.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1920, height: 1080 });
  await page.goto(`file:///${htmlPath.replace(/\\/g, '/')}`, { waitUntil: 'networkidle0', timeout: 30000 });
  await page.waitForSelector('svg', { timeout: 10000 });
  await page.screenshot({ path: pngPath, fullPage: true });
  await browser.close();
  console.log(`PNG generated: ${pngPath} (${require('fs').statSync(pngPath).size} bytes)`);
})();
