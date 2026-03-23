import { useTranslation } from 'react-i18next';
import { Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { motion } from 'framer-motion';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import heroImage from '@/assets/hero-collage.jpg';

const HeroSection = () => {
  const { t } = useTranslation('home');

  return (
    <section className="relative overflow-hidden gradient-hero">
      {/* Blobs */}
      <DecorativeBlob className="w-72 h-72 -top-20 -left-20" variant="primary" />
      <DecorativeBlob className="w-96 h-96 -bottom-40 -right-20" variant="secondary" />

      <div className="container mx-auto px-4 py-16 md:py-24 lg:py-32">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left: Content */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            <h1 className="font-heading text-4xl md:text-5xl lg:text-6xl font-bold leading-tight mb-6 text-foreground">
              {t('hero.title')}
            </h1>
            <p className="font-body text-lg text-muted-foreground mb-8 max-w-lg leading-relaxed">
              {t('hero.subtitle')}
            </p>

            {/* Search bar */}
            <div className="flex gap-2 max-w-lg mb-6">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                <Input
                  placeholder={t('hero.searchPlaceholder')}
                  className="pl-10 h-12 rounded-full font-body border-border bg-background"
                />
              </div>
              <Button size="lg" className="rounded-full px-8 font-body h-12">
                {t('common:buttons.discover')}
              </Button>
            </div>
          </motion.div>

          {/* Right: Image */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.8, delay: 0.2 }}
            className="relative hidden lg:block"
          >
            <div className="relative">
              <DecorativeBlob className="w-80 h-80 top-0 right-0" variant="accent" />
              <img
                src={heroImage}
                alt="Wellness community"
                className="relative rounded-3xl shadow-2xl w-full"
                width={1280}
                height={720}
              />
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
