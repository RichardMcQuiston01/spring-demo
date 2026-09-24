import './style.css';

function initMobileMenu(): void {
  const menuButton = document.querySelector<HTMLButtonElement>('[data-mobile-menu-button]');
  const menuPanel = document.querySelector<HTMLElement>('[data-mobile-menu]');

  if (!menuButton || !menuPanel) {
    return;
  }

  menuButton.addEventListener('click', () => {
    const isOpen = menuPanel.classList.contains('hidden');
    // Tailwind's `hidden` and `flex` utilities both set `display`, so only one
    // may be present at a time or the cascade order decides which one wins.
    menuPanel.classList.toggle('hidden', !isOpen);
    menuPanel.classList.toggle('flex', isOpen);
    menuButton.setAttribute('aria-expanded', String(isOpen));
  });
}

document.addEventListener('DOMContentLoaded', initMobileMenu);
