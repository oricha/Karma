import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import LanguageSwitcher from './LanguageSwitcher';

const Footer = () => {
  const { t } = useTranslation();

  return (
    <footer className="bg-foreground text-background">
      <div className="container mx-auto px-4 py-12 md:py-16">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          {/* Brand */}
          <div className="md:col-span-1">
            <h3 className="font-heading text-2xl font-bold mb-3">Karma</h3>
            <p className="text-background/70 text-sm font-body leading-relaxed">
              {t('footer.tagline')}
            </p>
          </div>

          {/* Karma Links */}
          <div>
            <h4 className="font-heading text-lg font-semibold mb-4">{t('footer.karma')}</h4>
            <ul className="space-y-2 font-body text-sm text-background/70">
              <li><Link to="/about" className="hover:text-background transition-colors">{t('footer.about')}</Link></li>
              <li><Link to="/help" className="hover:text-background transition-colors">{t('footer.help')}</Link></li>
              <li><Link to="/organize" className="hover:text-background transition-colors">{t('footer.organize')}</Link></li>
            </ul>
          </div>

          {/* Categories */}
          <div>
            <h4 className="font-heading text-lg font-semibold mb-4">{t('footer.categories')}</h4>
            <ul className="space-y-2 font-body text-sm text-background/70">
              <li><Link to="/events/category/talleres" className="hover:text-background transition-colors">Talleres</Link></li>
              <li><Link to="/events/category/ceremonias" className="hover:text-background transition-colors">Ceremonias</Link></li>
              <li><Link to="/events/category/danza" className="hover:text-background transition-colors">Danza</Link></li>
              <li><Link to="/events/category/musica" className="hover:text-background transition-colors">Música</Link></li>
            </ul>
          </div>

          {/* Language */}
          <div>
            <h4 className="font-heading text-lg font-semibold mb-4">{t('nav.language')}</h4>
            <LanguageSwitcher />
          </div>
        </div>

        <div className="border-t border-background/20 pt-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <p className="text-background/50 text-xs font-body">
            © {new Date().getFullYear()} Karma. {t('footer.rights')}
          </p>
          <div className="flex gap-4 text-background/50 text-xs font-body">
            <Link to="/terms" className="hover:text-background transition-colors">{t('footer.terms')}</Link>
            <Link to="/privacy" className="hover:text-background transition-colors">{t('footer.privacy')}</Link>
            <Link to="/cookies" className="hover:text-background transition-colors">{t('footer.cookies')}</Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
