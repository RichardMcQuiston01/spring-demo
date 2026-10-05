import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import type { Plugin } from 'vite';

const INCLUDE_DIRECTIVE: RegExp = /<!--\s*@include\s+([\w-]+)\s*-->/g;

export function includePartials(html: string, readPartial: (name: string) => string): string {
  return html.replace(INCLUDE_DIRECTIVE, (_directive: string, name: string) => readPartial(name));
}

/** Replaces `<!-- @include name -->` with the contents of `<partialsDir>/name.html`. */
export function htmlPartials(partialsDir: string): Plugin {
  const readPartial = (name: string): string => {
    const partialPath: string = resolve(partialsDir, `${name}.html`);
    try {
      return readFileSync(partialPath, 'utf-8');
    } catch (error) {
      throw new Error(`Cannot include partial '${name}': expected a file at ${partialPath}`, { cause: error });
    }
  };

  return {
    name: 'html-partials',
    transformIndexHtml: {
      order: 'pre',
      handler: (html: string): string => includePartials(html, readPartial),
    },
    configureServer(server) {
      server.watcher.add(partialsDir);
      server.watcher.on('change', (changedPath: string) => {
        if (changedPath.startsWith(partialsDir)) {
          server.ws.send({ type: 'full-reload' });
        }
      });
    },
  };
}
