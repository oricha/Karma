import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Menu, X, User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  DropdownMenuLabel,
} from '@/components/ui/dropdown-menu';
import LanguageSwitcher from './LanguageSwitcher';
import { useSession } from '@/hooks/use-session';
import { useAuthStore } from '@/lib/auth';

const Navbar = () => {
  const { t } = useTranslation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { user, isLoggedIn } = useSession();
  const clearSession = useAuthStore((state) => state.clearSession);

  return (
    <header className="sticky top-0 z-50 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80 border-b border-border">
      <nav className="container mx-auto flex items-center justify-between h-16 px-4">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2">
          <span className="font-heading text-2xl font-bold text-primary">Karma</span>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-6">
          <Link to="/events" className="font-body text-sm font-medium text-foreground hover:text-primary transition-colors">
            {t('nav.events')}
          </Link>
          <Link to="/groups" className="font-body text-sm font-medium text-foreground hover:text-primary transition-colors">
            {t('nav.groups')}
          </Link>
          <Link to="/about" className="font-body text-sm font-medium text-foreground hover:text-primary transition-colors">
            {t('nav.about')}
          </Link>
          <Link to="/help" className="font-body text-sm font-medium text-foreground hover:text-primary transition-colors">
            {t('nav.help')}
          </Link>
        </div>

        {/* Right side */}
        <div className="hidden md:flex items-center gap-3">
          <LanguageSwitcher />
          {isLoggedIn ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon" className="rounded-full">
                  <User className="h-5 w-5" />
                </Button>
              </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56">
                  <DropdownMenuLabel className="text-muted-foreground text-xs font-normal">
                  {t('nav.connectedAs')} {user?.email}
                  </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild><Link to="/account/groups">{t('nav.myGroups')}</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/account/events">{t('nav.myEvents')}</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/account/orders">{t('nav.orders')}</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/account/details">{t('nav.accountDetails')}</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/account/preferences">{t('nav.preferences')}</Link></DropdownMenuItem>
                <DropdownMenuItem asChild><Link to="/account/saved-events">{t('nav.savedEvents')}</Link></DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuLabel className="text-xs font-medium">{t('nav.organizerSection')}</DropdownMenuLabel>
                <DropdownMenuItem asChild><Link to="/organizer/dashboard">{t('nav.organizeEvent')}</Link></DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-destructive" onClick={clearSession}>{t('nav.logout')}</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost" size="sm" className="font-body">{t('nav.login')}</Button>
              </Link>
              <Link to="/register">
                <Button size="sm" className="font-body rounded-full">{t('nav.register')}</Button>
              </Link>
            </>
          )}
        </div>

        {/* Mobile menu toggle */}
        <button
          className="md:hidden p-2"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
        </button>
      </nav>

      {/* Mobile menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t border-border bg-background px-4 py-4 space-y-3">
          <Link to="/events" className="block py-2 font-body text-sm" onClick={() => setMobileMenuOpen(false)}>{t('nav.events')}</Link>
          <Link to="/groups" className="block py-2 font-body text-sm" onClick={() => setMobileMenuOpen(false)}>{t('nav.groups')}</Link>
          <Link to="/about" className="block py-2 font-body text-sm" onClick={() => setMobileMenuOpen(false)}>{t('nav.about')}</Link>
          <Link to="/help" className="block py-2 font-body text-sm" onClick={() => setMobileMenuOpen(false)}>{t('nav.help')}</Link>
          <div className="pt-2 border-t border-border flex items-center gap-2">
            <LanguageSwitcher />
            {isLoggedIn ? (
              <Button variant="ghost" size="sm" className="font-body" onClick={clearSession}>{t('nav.logout')}</Button>
            ) : (
              <>
                <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                  <Button variant="ghost" size="sm" className="font-body">{t('nav.login')}</Button>
                </Link>
                <Link to="/register" onClick={() => setMobileMenuOpen(false)}>
                  <Button size="sm" className="font-body rounded-full">{t('nav.register')}</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;
