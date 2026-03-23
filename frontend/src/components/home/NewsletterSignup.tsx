import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import DecorativeBlob from '@/components/common/DecorativeBlob';

const NewsletterSignup = () => {
  const { t } = useTranslation('home');

  return (
    <section className="section-padding gradient-section-cool relative overflow-hidden">
      <DecorativeBlob className="w-72 h-72 -bottom-20 -left-20" variant="primary" />
      <div className="container mx-auto px-4 text-center relative">
        <h2 className="font-heading text-3xl font-bold mb-3">{t('newsletter.title')}</h2>
        <p className="font-body text-muted-foreground mb-8 max-w-md mx-auto">{t('newsletter.subtitle')}</p>
        <div className="flex gap-2 max-w-md mx-auto mb-4">
          <Input
            type="email"
            placeholder={t('newsletter.placeholder')}
            className="rounded-full h-12 font-body"
          />
          <Button size="lg" className="rounded-full font-body h-12 px-8">
            {t('common:buttons.subscribe')}
          </Button>
        </div>
        <p className="text-xs font-body text-muted-foreground max-w-sm mx-auto">{t('newsletter.consent')}</p>
      </div>
    </section>
  );
};

export default NewsletterSignup;
