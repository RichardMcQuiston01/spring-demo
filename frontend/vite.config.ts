import { resolve } from 'node:path';
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [tailwindcss()],
  build: {
    rollupOptions: {
      input: {
        main: resolve(import.meta.dirname, 'index.html'),
        about: resolve(import.meta.dirname, 'about.html'),
        contact: resolve(import.meta.dirname, 'contact.html'),
        productCoasterSet: resolve(import.meta.dirname, 'products/engraved-coaster-set.html'),
        productWallMap: resolve(import.meta.dirname, 'products/layered-wall-map.html'),
      },
    },
  },
});
