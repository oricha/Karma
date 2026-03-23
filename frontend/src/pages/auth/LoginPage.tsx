import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { useMutation } from '@tanstack/react-query';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import { api } from '@/lib/api';
import { useAuthStore } from '@/lib/auth';

const LoginPage = () => {
  const { t } = useTranslation('auth');
  const navigate = useNavigate();
  const location = useLocation();
  const setSession = useAuthStore((state) => state.setSession);
  const [email, setEmail] = useState('demo@karma.app');
  const [password, setPassword] = useState('demo123');
  const mutation = useMutation({
    mutationFn: api.login,
    onSuccess: (response) => {
      setSession(response);
      navigate((location.state as { from?: string } | null)?.from ?? '/account/events');
    },
  });

  return (
    <>
      <Helmet><title>{t('login.title')} — Karma</title></Helmet>
      <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden">
        <DecorativeBlob className="w-96 h-96 -top-40 -right-40" variant="primary" />
        <DecorativeBlob className="w-72 h-72 -bottom-20 -left-20" variant="secondary" />

        <div className="w-full max-w-md p-8 bg-card rounded-3xl shadow-lg relative z-10 mx-4">
          <div className="text-center mb-8">
            <h1 className="font-heading text-3xl font-bold mb-2">{t('login.title')}</h1>
            <p className="font-body text-muted-foreground">{t('login.subtitle')}</p>
          </div>

          <form className="space-y-4" onSubmit={(e) => {
            e.preventDefault();
            mutation.mutate({ email, password });
          }}>
            <div>
              <Label htmlFor="email" className="font-body">{t('login.email')}</Label>
              <Input id="email" type="email" className="mt-1 rounded-lg font-body" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div>
              <Label htmlFor="password" className="font-body">{t('login.password')}</Label>
              <Input id="password" type="password" className="mt-1 rounded-lg font-body" value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>

            <div className="text-right">
              <Link to="/forgot-password" className="text-sm font-body text-primary hover:text-primary-hover">
                {t('login.forgotPassword')}
              </Link>
            </div>

            <Button type="submit" className="w-full rounded-full font-body h-12">
              {t('login.submit')}
            </Button>
            {mutation.isError ? (
              <p className="text-sm text-destructive">{(mutation.error as Error).message}</p>
            ) : null}
          </form>

          <p className="text-center mt-6 font-body text-sm text-muted-foreground">
            {t('login.noAccount')}{' '}
            <Link to="/register" className="text-primary hover:text-primary-hover font-medium">
              {t('login.register')}
            </Link>
          </p>
        </div>
      </div>
    </>
  );
};

export default LoginPage;
