const HOME_HREF: string = '/';
const SHOP_HREF: string = '/#shop';

/** Product pages belong to the Shop link; every other page links to itself. */
export function resolveCurrentNavHref(pathname: string): string {
  if (pathname === '/' || pathname === '/index.html') {
    return HOME_HREF;
  }
  if (pathname.startsWith('/products/')) {
    return SHOP_HREF;
  }
  return pathname;
}

/** Marks the nav links for the current page with aria-current and returns how many were marked. */
export function markCurrentNavLink(root: ParentNode, pathname: string): number {
  const currentHref: string = resolveCurrentNavHref(pathname);
  let markedCount: number = 0;

  for (const link of root.querySelectorAll<HTMLAnchorElement>('a[data-nav]')) {
    if (link.getAttribute('href') === currentHref) {
      link.setAttribute('aria-current', 'page');
      markedCount++;
    }
  }

  return markedCount;
}
