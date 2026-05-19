import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import { api } from '@/lib/api';
import { blogContent, blogTitle } from '@/lib/blog';

const BlogDetailPage = () => {
  const { slug } = useParams();
  const { t, i18n } = useTranslation('home');
  const { data: post, isLoading } = useQuery({
    queryKey: ['blog-post', slug],
    queryFn: () => api.getBlogPost(slug!),
    enabled: Boolean(slug),
  });

  if (isLoading || !post) {
    return (
      <section className="container mx-auto px-4 py-12">
        <p className="font-body text-muted-foreground">{t('common:noResults')}</p>
      </section>
    );
  }

  const title = blogTitle(post, i18n.language);
  const body = blogContent(post, i18n.language);

  return (
    <>
      <Helmet><title>{title} — Karma</title></Helmet>
      <article className="container mx-auto px-4 py-12 max-w-3xl">
        <Link to="/blog" className="inline-flex items-center gap-2 font-body text-sm text-muted-foreground hover:text-primary mb-8">
          <ArrowLeft className="h-4 w-4" />
          {t('community.title')}
        </Link>
        {post.coverImageUrl ? (
          <img src={post.coverImageUrl} alt={title} className="w-full rounded-2xl mb-8 aspect-[16/9] object-cover" />
        ) : null}
        <h1 className="font-heading text-3xl md:text-4xl font-bold mb-6">{title}</h1>
        <div className="prose prose-neutral max-w-none font-body whitespace-pre-wrap leading-relaxed">
          {body}
        </div>
      </article>
    </>
  );
};

export default BlogDetailPage;
