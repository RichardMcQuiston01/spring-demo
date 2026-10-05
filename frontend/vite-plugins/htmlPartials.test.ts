import { describe, expect, it } from 'vitest';
import { includePartials } from './htmlPartials.ts';

const partials: Record<string, string> = {
  header: '<header>Header</header>',
  footer: '<footer>Footer</footer>',
};

function readPartial(name: string): string {
  const partial: string | undefined = partials[name];
  if (partial === undefined) {
    throw new Error(`Unknown partial '${name}'`);
  }
  return partial;
}

describe('includePartials', () => {
  it('replaces include directives with the partial contents', () => {
    const html: string = '<body>\n<!-- @include header -->\n<main></main>\n<!-- @include footer -->\n</body>';

    expect(includePartials(html, readPartial)).toBe(
      '<body>\n<header>Header</header>\n<main></main>\n<footer>Footer</footer>\n</body>',
    );
  });

  it('tolerates extra whitespace inside the directive', () => {
    expect(includePartials('<!--   @include   header   -->', readPartial)).toBe('<header>Header</header>');
  });

  it('leaves ordinary comments and html untouched', () => {
    const html: string = '<!-- just a comment --><p>Hi</p>';

    expect(includePartials(html, readPartial)).toBe(html);
  });

  it('surfaces the error when a partial cannot be read', () => {
    expect(() => includePartials('<!-- @include missing -->', readPartial)).toThrow("Unknown partial 'missing'");
  });
});
