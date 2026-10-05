import { readdirSync } from 'node:fs';
import { basename, resolve } from 'node:path';

/** Maps every `.html` file directly inside the given directories to a Rollup entry. */
export function findHtmlEntries(rootDir: string, entryDirs: readonly string[]): Record<string, string> {
  const entries: Record<string, string> = {};

  for (const entryDir of entryDirs) {
    const absoluteDir: string = resolve(rootDir, entryDir);
    for (const fileName of readdirSync(absoluteDir)) {
      if (!fileName.endsWith('.html')) {
        continue;
      }
      const entryName: string = [entryDir === '.' ? '' : entryDir, basename(fileName, '.html')]
        .filter(Boolean)
        .join('-');
      entries[entryName] = resolve(absoluteDir, fileName);
    }
  }

  return entries;
}
