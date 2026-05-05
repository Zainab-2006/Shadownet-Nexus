import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';

const trackedFiles = execFileSync('git', ['ls-files'], { encoding: 'utf8' })
  .split(/\r?\n/)
  .filter(Boolean);

const blockedTrackedFiles = new Set(['.env', 'springboot/.env']);
const ignoredExtensions = new Set([
  '.png',
  '.jpg',
  '.jpeg',
  '.gif',
  '.webp',
  '.ico',
  '.jar',
  '.zip',
]);

const secretPatterns = [
  { name: 'private key', pattern: /-----BEGIN (?:RSA |EC |OPENSSH |DSA )?PRIVATE KEY-----/ },
  { name: 'aws access key', pattern: /\bAKIA[0-9A-Z]{16}\b/ },
  { name: 'github token', pattern: /\bgh[pousr]_[A-Za-z0-9_]{36,255}\b/ },
  { name: 'slack token', pattern: /\bxox[baprs]-[A-Za-z0-9-]{20,}\b/ },
  { name: 'openai api key', pattern: /\bsk-[A-Za-z0-9_-]{32,}\b/ },
  { name: 'jwt token', pattern: /\beyJ[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\.[A-Za-z0-9_-]{20,}\b/ },
];

const getExtension = (file) => {
  const lastDot = file.lastIndexOf('.');
  return lastDot === -1 ? '' : file.slice(lastDot).toLowerCase();
};

const findings = [];

for (const file of trackedFiles) {
  if (blockedTrackedFiles.has(file.replaceAll('\\', '/'))) {
    findings.push(`${file}: local environment file must not be tracked`);
    continue;
  }

  if (ignoredExtensions.has(getExtension(file))) {
    continue;
  }

  let content;
  try {
    content = readFileSync(file, 'utf8');
  } catch {
    continue;
  }

  for (const { name, pattern } of secretPatterns) {
    if (pattern.test(content)) {
      findings.push(`${file}: possible ${name}`);
    }
  }
}

if (findings.length > 0) {
  console.error('Security scan failed:');
  for (const finding of findings) {
    console.error(`- ${finding}`);
  }
  process.exit(1);
}

console.log(`Security scan clean (${trackedFiles.length} tracked files checked).`);
