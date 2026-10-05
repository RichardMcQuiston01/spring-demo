import { beforeEach, describe, expect, it } from 'vitest';
import { markCurrentNavLink, resolveCurrentNavHref } from './navigation.ts';

describe('resolveCurrentNavHref', () => {
  it.each([
    ['/', '/'],
    ['/index.html', '/'],
    ['/about.html', '/about.html'],
    ['/contact.html', '/contact.html'],
    ['/products/layered-wall-map.html', '/#shop'],
  ])('maps %s to %s', (pathname: string, expectedHref: string) => {
    expect(resolveCurrentNavHref(pathname)).toBe(expectedHref);
  });
});

describe('markCurrentNavLink', () => {
  beforeEach(() => {
    document.body.innerHTML = `
      <nav>
        <a href="/" data-nav>Home</a>
        <a href="/#shop" data-nav>Shop</a>
        <a href="/about.html" data-nav>About</a>
      </nav>
      <nav>
        <a href="/about.html" data-nav>About (mobile)</a>
      </nav>
      <a href="/about.html">Call to action, not part of the nav</a>
    `;
  });

  it('marks every nav link pointing at the current page and nothing else', () => {
    const markedCount: number = markCurrentNavLink(document, '/about.html');

    const markedTexts: string[] = Array.from(document.querySelectorAll('[aria-current="page"]'))
      .map((link: Element) => link.textContent ?? '');
    expect(markedCount).toBe(2);
    expect(markedTexts).toEqual(['About', 'About (mobile)']);
  });

  it('marks Shop on product pages', () => {
    markCurrentNavLink(document, '/products/layered-wall-map.html');

    expect(document.querySelector('[aria-current="page"]')?.textContent).toBe('Shop');
  });

  it('marks nothing and returns 0 for a page with no nav link', () => {
    expect(markCurrentNavLink(document, '/missing.html')).toBe(0);
    expect(document.querySelector('[aria-current]')).toBeNull();
  });
});
