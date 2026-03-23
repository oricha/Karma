import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import type { Category } from '@/types';

const CategoryCard = ({ category }: { category: Category }) => {
  const { i18n } = useTranslation();
  const name = i18n.language === 'es' ? category.nameEs : category.nameEn;

  return (
    <Link to={`/events/category/${category.slug}`} className="group block">
      <div className="relative rounded-2xl overflow-hidden aspect-[4/3] shadow-sm hover:shadow-md transition-all group-hover:-translate-y-1">
        <img
          src={category.imageUrl}
          alt={name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-foreground/70 via-foreground/20 to-transparent" />
        <div className="absolute bottom-4 left-4 right-4">
          <h3 className="font-heading text-xl font-semibold text-background mb-1">{name}</h3>
          {category.eventCount !== undefined && (
            <p className="font-body text-sm text-background/80">{category.eventCount} eventos</p>
          )}
        </div>
      </div>
    </Link>
  );
};

export default CategoryCard;
