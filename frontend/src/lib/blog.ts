import type { BlogPost } from '@/types';

export function blogTitle(post: BlogPost, language: string) {
  if (language === 'ca' && post.titleCa) return post.titleCa;
  if (language === 'en') return post.titleEn;
  return post.titleEs;
}

export function blogExcerpt(post: BlogPost, language: string) {
  if (language === 'ca' && post.excerptCa) return post.excerptCa;
  if (language === 'en') return post.excerptEn;
  return post.excerptEs;
}

export function blogContent(post: BlogPost, language: string) {
  if (language === 'ca' && post.contentCa) return post.contentCa;
  if (language === 'en') return post.contentEn;
  return post.contentEs;
}
