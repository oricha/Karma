import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Users, Calendar, ShoppingBag, User, Settings, Heart } from 'lucide-react';
import { useSession } from '@/hooks/use-session';

const AccountSidebar = () => {
  const { t } = useTranslation('account');
  const location = useLocation();
  const { user } = useSession();

  const navItems = [
    { to: '/account/groups', label: t('sidebar.myGroups'), icon: Users },
    { to: '/account/events', label: t('sidebar.myEvents'), icon: Calendar },
    { to: '/account/orders', label: t('sidebar.orders'), icon: ShoppingBag },
    { to: '/account/details', label: t('sidebar.details'), icon: User },
    { to: '/account/preferences', label: t('sidebar.preferences'), icon: Settings },
    { to: '/account/saved-events', label: t('sidebar.savedEvents'), icon: Heart },
  ];

  return (
    <aside className="w-full md:w-64 shrink-0">
      {/* User info */}
      <div className="flex items-center gap-3 mb-6 p-4 bg-card rounded-lg">
        <div className="w-12 h-12 rounded-full bg-primary-light flex items-center justify-center">
          <User className="h-6 w-6 text-primary" />
        </div>
        <div>
          <p className="font-body font-semibold text-sm">{user ? `${user.firstName} ${user.lastName}` : 'Karma'}</p>
          <p className="font-body text-xs text-muted-foreground">{user?.email ?? ''}</p>
        </div>
      </div>

      {/* Nav items */}
      <nav className="space-y-1">
        {navItems.map(item => {
          const isActive = location.pathname === item.to;
          return (
            <Link
              key={item.to}
              to={item.to}
              className={`flex items-center gap-3 px-4 py-2.5 rounded-lg font-body text-sm transition-colors ${
                isActive
                  ? 'bg-accent text-accent-foreground border-l-4 border-primary font-medium'
                  : 'text-muted-foreground hover:bg-muted hover:text-foreground'
              }`}
            >
              <item.icon className="h-4 w-4" />
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
};

export default AccountSidebar;
