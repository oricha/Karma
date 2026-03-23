import { useTranslation } from 'react-i18next';
import { useEffect, useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import AccountLayout from '@/components/layout/AccountLayout';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/auth';

const AccountDetailsPage = () => {
  const { t, i18n } = useTranslation(['account', 'common']);
  const { data: user } = useQuery({ queryKey: ['current-user'], queryFn: api.getCurrentUser });
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', phone: '', bio: '', locale: 'es' });
  const setSession = useAuthStore((state) => state.setSession);
  const accessToken = useAuthStore((state) => state.accessToken);
  const refreshToken = useAuthStore((state) => state.refreshToken);
  const mutation = useMutation({
    mutationFn: api.updateCurrentUser,
    onSuccess: (updatedUser) => {
      if (accessToken && refreshToken) {
        setSession({ accessToken, refreshToken, user: updatedUser });
      }
      void i18n.changeLanguage(updatedUser.locale);
    },
  });

  useEffect(() => {
    if (user) {
      setForm({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone ?? '',
        bio: user.bio ?? '',
        locale: user.locale,
      });
    }
  }, [user]);

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('details.title')}</h1>
      <form className="bg-card rounded-xl p-6 space-y-4 max-w-lg" onSubmit={(e) => {
        e.preventDefault();
        mutation.mutate(form);
      }}>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label className="font-body">{t('details.firstName')}</Label>
            <Input className="mt-1 rounded-lg font-body" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
          </div>
          <div>
            <Label className="font-body">{t('details.lastName')}</Label>
            <Input className="mt-1 rounded-lg font-body" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          </div>
        </div>
        <div>
          <Label className="font-body">{t('details.email')}</Label>
          <Input className="mt-1 rounded-lg font-body" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </div>
        <div>
          <Label className="font-body">{t('details.phone')}</Label>
          <Input className="mt-1 rounded-lg font-body" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <div>
          <Label className="font-body">{t('details.bio')}</Label>
          <Textarea className="mt-1 rounded-lg font-body" rows={3} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
        </div>
        <div>
          <Label className="font-body">{t('details.language')}</Label>
          <select
            className="mt-1 flex h-10 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm"
            value={form.locale}
            onChange={(e) => setForm({ ...form, locale: e.target.value })}
          >
            <option value="es">Español</option>
            <option value="en">English</option>
          </select>
        </div>
        {!user?.emailVerified ? (
          <p className="text-sm text-amber-700">{t('details.emailNotVerified')}</p>
        ) : null}
        <Button type="submit" className="rounded-full font-body">
          {t('common:buttons.saveChanges')}
        </Button>
      </form>
    </AccountLayout>
  );
};

export default AccountDetailsPage;
