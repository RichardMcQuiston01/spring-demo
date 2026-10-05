const MENU_BUTTON_SELECTOR: string = '[data-mobile-menu-button]';
const MENU_PANEL_SELECTOR: string = '[data-mobile-menu]';

/** Wires up the menu toggle; returns false when the page has no mobile menu markup. */
export function initMobileMenu(root: ParentNode): boolean {
  const menuButton: HTMLButtonElement | null = root.querySelector(MENU_BUTTON_SELECTOR);
  const menuPanel: HTMLElement | null = root.querySelector(MENU_PANEL_SELECTOR);

  if (!menuButton || !menuPanel) {
    return false;
  }

  menuButton.addEventListener('click', () => {
    const isOpening: boolean = menuPanel.classList.contains('hidden');
    // Tailwind's `hidden` and `flex` both set `display`, so only one may be present or cascade order decides.
    menuPanel.classList.toggle('hidden', !isOpening);
    menuPanel.classList.toggle('flex', isOpening);
    menuButton.setAttribute('aria-expanded', String(isOpening));
  });

  return true;
}
