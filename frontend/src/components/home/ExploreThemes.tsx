import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import CategoryCard from '@/components/categories/CategoryCard';
import { api } from '@/lib/api';

const ExploreThemes = () => {
  const { t } = useTranslation('home');
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: api.getCategories });

  return (
    <section className="section-padding gradient-section-warm">
      <div className="container mx-auto px-4">
        <div className="text-center mb-10">
          <h2 className="font-heading text-3xl font-bold mb-3">{t('exploreThemes.title')}</h2>
          <p className="font-body text-muted-foreground">{t('exploreThemes.subtitle')}</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {categories.map(category => (
            <CategoryCard key={category.id} category={category} />
          ))}
        </div>
      </div>
    </section>
  );
};

export default ExploreThemes;
