import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '@/lib/api';

const CommunitySection = () => {
  const { t, i18n } = useTranslation('home');
  const { data: posts = [] } = useQuery({ queryKey: ['featured-blog-posts'], queryFn: api.getFeaturedBlogPosts });

  return (
    <section className="section-padding">
      <div className="container mx-auto px-4">
        <h2 className="font-heading text-3xl font-bold text-center mb-10">{t('community.title')}</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {posts.map(post => (
            <Link key={post.id} to={`/blog/${post.slug}`} className="group block">
              <div className="bg-card rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-all group-hover:-translate-y-1">
                <div className="aspect-[16/10] overflow-hidden">
                  <img
                    src={post.coverImageUrl}
                    alt={i18n.language === 'es' ? post.titleEs : post.titleEn}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    loading="lazy"
                  />
                </div>
                <div className="p-4">
                  <h3 className="font-heading text-lg font-semibold mb-2 group-hover:text-primary transition-colors">
                    {i18n.language === 'es' ? post.titleEs : post.titleEn}
                  </h3>
                  <p className="font-body text-sm text-muted-foreground line-clamp-2">
                    {i18n.language === 'es' ? post.excerptEs : post.excerptEn}
                  </p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </section>
  );
};

export default CommunitySection;
