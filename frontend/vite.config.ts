import { resolve } from 'node:path';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vitest/config';
import { findHtmlEntries } from './vite-plugins/htmlEntries.ts';
import { htmlPartials } from './vite-plugins/htmlPartials.ts';

const projectRoot: string = import.meta.dirname;

export default defineConfig({
  plugins: [htmlPartials(resolve(projectRoot, 'partials')), tailwindcss()],
  build: {
    rollupOptions: {
      input: findHtmlEntries(projectRoot, ['.', 'products']),
    },
  },
  test: {
    environment: 'jsdom',
  },
});
