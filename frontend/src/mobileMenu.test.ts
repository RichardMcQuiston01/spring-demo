import { beforeEach, describe, expect, it } from 'vitest';
import { initMobileMenu } from './mobileMenu.ts';

const MENU_MARKUP: string = `
  <button data-mobile-menu-button aria-expanded="false">Menu</button>
  <nav data-mobile-menu class="hidden flex-col"></nav>
`;

function getMenuParts(): { button: HTMLButtonElement; panel: HTMLElement } {
  return {
    button: document.querySelector<HTMLButtonElement>('[data-mobile-menu-button]')!,
    panel: document.querySelector<HTMLElement>('[data-mobile-menu]')!,
  };
}

describe('initMobileMenu', () => {
  beforeEach(() => {
    document.body.innerHTML = MENU_MARKUP;
  });

  it('returns false when the page has no mobile menu markup', () => {
    document.body.innerHTML = '<p>No menu here</p>';

    expect(initMobileMenu(document)).toBe(false);
  });

  it('returns true when the menu markup is present', () => {
    expect(initMobileMenu(document)).toBe(true);
  });

  it('opens on the first click, swapping hidden for flex', () => {
    initMobileMenu(document);
    const { button, panel } = getMenuParts();

    button.click();

    expect(panel.classList.contains('hidden')).toBe(false);
    expect(panel.classList.contains('flex')).toBe(true);
    expect(button.getAttribute('aria-expanded')).toBe('true');
  });

  it('closes again on the second click', () => {
    initMobileMenu(document);
    const { button, panel } = getMenuParts();

    button.click();
    button.click();

    expect(panel.classList.contains('hidden')).toBe(true);
    expect(panel.classList.contains('flex')).toBe(false);
    expect(button.getAttribute('aria-expanded')).toBe('false');
  });
});
