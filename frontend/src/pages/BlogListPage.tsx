import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import { blogExcerpt, blogTitle } from '@/lib/blog';

const BlogListPage = () => {
  const { t, i18n } = useTranslation('home');
  const { data: posts = [], isLoading } = useQuery({ queryKey: ['blog-posts'], queryFn: api.getBlogPosts });

  return (
    <>
      <Helmet><title>{t('community.title')} — Karma</title></Helmet>
      <section className="container mx-auto px-4 py-12">
        <h1 className="font-heading text-3xl md:text-4xl font-bold mb-8">{t('community.title')}</h1>
        {isLoading ? (
          <p className="font-body text-muted-foreground">{t('common:noResults')}</p>
        ) : posts.length === 0 ? (
          <p className="font-body text-muted-foreground">{t('common:noResults')}</p>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 list-none p-0 m-0">
            {posts.map(post => (
              <li key={post.id}>
                <Link to={`/blog/${post.slug}`} className="group block h-full">
                  <article className="bg-card rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all group-hover:-translate-y-1 h-full">
                    {post.coverImageUrl ? (
                      <figure className="aspect-[16/10] overflow-hidden m-0">
                        <img
                          src={post.coverImageUrl}
                          alt={blogTitle(post, i18n.language)}
                          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                          loading="lazy"
                        />
                      </figure>
                    ) : null}
                    <div className="p-5">
                      <h2 className="font-heading text-lg font-semibold mb-2 group-hover:text-primary transition-colors">
                        {blogTitle(post, i18n.language)}
                      </h2>
                      <p className="font-body text-sm text-muted-foreground line-clamp-3">
                        {blogExcerpt(post, i18n.language)}
                      </p>
                    </div>
                  </article>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
};

export default BlogListPage;
