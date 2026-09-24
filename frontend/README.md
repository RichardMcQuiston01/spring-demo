# Sawdust & Co. — storefront demo

A very small front-facing storefront demo: a home page with a couple of
product pages, an About page, and a Contact page that links straight to
email instead of a form. No cart, no checkout, no backend — just a clean,
responsive static site.

"Sawdust & Co." and its products are fictional, built to have something
believable to show rather than lorem ipsum.

## Tech stack

- [Vite](https://vite.dev) (multi-page build, one HTML entry point per page)
- TypeScript
- Tailwind CSS v4 (via `@tailwindcss/vite`)

## Pages

- `index.html` — home page with a product grid (`#shop`)
- `products/engraved-coaster-set.html`, `products/layered-wall-map.html` —
  product detail pages, each with an "Email to order" link instead of a cart
- `about.html`
- `contact.html` — `mailto:` links only, no contact form

## Running locally

```bash
npm install
npm run dev
```

Then open the printed local URL. `npm run build` type-checks and produces a
static `dist/` you can serve from any static host (or the Hostinger static
site / Node.js hosting tools); `npm run preview` serves that build locally.

## Notes

- All product imagery is original inline SVG illustration — no stock photos
  or external image hosts, so the site has zero external asset dependencies.
- The email addresses on the Contact and product pages are placeholders
  (`@sawdustandco.example`) and aren't monitored inboxes.
