const IMAGE_PATTERN = /!\[([^\]]*)\]\(([^)\s]+)\)/g;

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function isSafeImageUrl(url: string): boolean {
  if (url.startsWith('/uploads/')) return true;
  try {
    const u = new URL(url);
    return u.protocol === 'https:' || u.protocol === 'http:';
  } catch {
    return false;
  }
}

export function renderImageMarkdown(content: string): string {
  let out = '';
  let lastIndex = 0;
  for (const m of content.matchAll(IMAGE_PATTERN)) {
    const start = m.index ?? 0;
    out += escapeHtml(content.substring(lastIndex, start)).replace(/\n/g, '<br>');
    const alt = m[1] ?? '';
    const url = m[2] ?? '';
    if (isSafeImageUrl(url)) {
      out += `<img src="${escapeHtml(url)}" alt="${escapeHtml(alt)}" class="my-3 max-w-full rounded-md" loading="lazy" />`;
    } else {
      out += escapeHtml(m[0]);
    }
    lastIndex = start + m[0].length;
  }
  out += escapeHtml(content.substring(lastIndex)).replace(/\n/g, '<br>');
  return out;
}
