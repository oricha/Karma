import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useMutation } from '@tanstack/react-query';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/auth';

const RegisterPage = () => {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const setSession = useAuthStore((state) => state.setSession);
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', confirmPassword: '' });
  const mutation = useMutation({
    mutationFn: api.register,
    onSuccess: (response) => {
      setSession(response);
      if (response.emailVerificationToken) {
        navigate(`/verify-email?token=${response.emailVerificationToken}`);
        return;
      }
      navigate('/account/details');
    },
  });

  return (
    <>
      <Helmet><title>{t('register.title')} — Karma</title></Helmet>
      <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden py-8">
        <DecorativeBlob className="w-96 h-96 -top-40 -left-40" variant="secondary" />
        <DecorativeBlob className="w-72 h-72 -bottom-20 -right-20" variant="primary" />

        <div className="w-full max-w-md p-8 bg-card rounded-3xl shadow-lg relative z-10 mx-4">
          <div className="text-center mb-8">
            <h1 className="font-heading text-3xl font-bold mb-2">{t('register.title')}</h1>
            <p className="font-body text-muted-foreground">{t('register.subtitle')}</p>
          </div>

          <form className="space-y-4" onSubmit={(e) => {
            e.preventDefault();
            if (form.password !== form.confirmPassword) return;
            mutation.mutate({
              email: form.email,
              password: form.password,
              firstName: form.firstName,
              lastName: form.lastName,
            });
          }}>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="firstName" className="font-body">{t('register.firstName')}</Label>
                <Input id="firstName" className="mt-1 rounded-lg font-body" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
              </div>
              <div>
                <Label htmlFor="lastName" className="font-body">{t('register.lastName')}</Label>
                <Input id="lastName" className="mt-1 rounded-lg font-body" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
              </div>
            </div>
            <div>
              <Label htmlFor="email" className="font-body">{t('register.email')}</Label>
              <Input id="email" type="email" className="mt-1 rounded-lg font-body" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div>
              <Label htmlFor="password" className="font-body">{t('register.password')}</Label>
              <Input id="password" type="password" className="mt-1 rounded-lg font-body" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
            </div>
            <div>
              <Label htmlFor="confirmPassword" className="font-body">{t('register.confirmPassword')}</Label>
              <Input id="confirmPassword" type="password" className="mt-1 rounded-lg font-body" value={form.confirmPassword} onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} />
            </div>

            <Button type="submit" className="w-full rounded-full font-body h-12">
              {t('register.submit')}
            </Button>
            {form.password !== form.confirmPassword ? (
              <p className="text-sm text-destructive">{t('resetPassword.mismatch')}</p>
            ) : null}
            {mutation.isError ? (
              <p className="text-sm text-destructive">{(mutation.error as Error).message}</p>
            ) : null}
          </form>

          <p className="text-center mt-6 font-body text-sm text-muted-foreground">
            {t('register.hasAccount')}{' '}
            <Link to="/login" className="text-primary hover:text-primary-hover font-medium">
              {t('register.login')}
            </Link>
          </p>
        </div>
      </div>
    </>
  );
};

export default RegisterPage;
