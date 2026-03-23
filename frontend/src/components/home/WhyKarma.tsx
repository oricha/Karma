import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { Ticket, Target, Heart } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import DecorativeBlob from '@/components/common/DecorativeBlob';

const WhyKarma = () => {
  const { t } = useTranslation('home');

  const features = [
    { icon: Ticket, titleKey: 'whyKarma.feature1.title', descKey: 'whyKarma.feature1.description' },
    { icon: Target, titleKey: 'whyKarma.feature2.title', descKey: 'whyKarma.feature2.description' },
    { icon: Heart, titleKey: 'whyKarma.feature3.title', descKey: 'whyKarma.feature3.description' },
  ];

  return (
    <section className="section-padding relative overflow-hidden">
      <DecorativeBlob className="w-64 h-64 -top-20 right-0" variant="secondary" />
      <div className="container mx-auto px-4">
        <h2 className="font-heading text-3xl font-bold text-center mb-12">{t('whyKarma.title')}</h2>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          {features.map((feature, i) => (
            <motion.div
              key={i}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.15 }}
              className="text-center"
            >
              <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-primary-light mb-4">
                <feature.icon className="h-8 w-8 text-primary" />
              </div>
              <h3 className="font-heading text-xl font-semibold mb-2">{t(feature.titleKey)}</h3>
              <p className="font-body text-muted-foreground text-sm leading-relaxed">{t(feature.descKey)}</p>
            </motion.div>
          ))}
        </div>

        {/* CTA Card */}
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          whileInView={{ opacity: 1, scale: 1 }}
          viewport={{ once: true }}
          className="bg-primary rounded-3xl p-8 md:p-12 text-center text-primary-foreground max-w-2xl mx-auto"
        >
          <h3 className="font-heading text-2xl md:text-3xl font-bold mb-3">{t('whyKarma.cta.title')}</h3>
          <p className="font-body text-primary-foreground/80 mb-6">{t('whyKarma.cta.subtitle')}</p>
          <Link to="/organize">
            <Button size="lg" variant="secondary" className="rounded-full font-body px-8">
              {t('common:buttons.startToday')}
            </Button>
          </Link>
        </motion.div>
      </div>
    </section>
  );
};

export default WhyKarma;
