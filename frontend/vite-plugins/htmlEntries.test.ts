import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { findHtmlEntries } from './htmlEntries.ts';

describe('findHtmlEntries', () => {
  let rootDir: string;

  beforeEach(() => {
    rootDir = mkdtempSync(join(tmpdir(), 'html-entries-'));
    mkdirSync(join(rootDir, 'products'));
    mkdirSync(join(rootDir, 'partials'));
    writeFileSync(join(rootDir, 'index.html'), '');
    writeFileSync(join(rootDir, 'about.html'), '');
    writeFileSync(join(rootDir, 'notes.txt'), '');
    writeFileSync(join(rootDir, 'products', 'widget.html'), '');
    writeFileSync(join(rootDir, 'partials', 'header.html'), '');
  });

  afterEach(() => {
    rmSync(rootDir, { recursive: true, force: true });
  });

  it('collects html files from the requested directories only', () => {
    const entries: Record<string, string> = findHtmlEntries(rootDir, ['.', 'products']);

    expect(entries).toEqual({
      index: join(rootDir, 'index.html'),
      about: join(rootDir, 'about.html'),
      'products-widget': join(rootDir, 'products', 'widget.html'),
    });
  });
});
