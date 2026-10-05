import './style.css';
import { initMobileMenu } from './mobileMenu.ts';
import { markCurrentNavLink } from './navigation.ts';

document.addEventListener('DOMContentLoaded', () => {
  if (!initMobileMenu(document)) {
    console.warn('Mobile menu markup was not found on this page, so the menu toggle was not wired up.');
  }
  markCurrentNavLink(document, window.location.pathname);
});
