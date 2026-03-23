import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { useTranslation } from 'react-i18next';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { api } from '@/lib/api';

const ForgotPasswordPage = () => {
  const { t } = useTranslation('auth');
  const [email, setEmail] = useState('demo@karma.app');
  const mutation = useMutation({
    mutationFn: api.forgotPassword,
  });

  return (
    <>
      <Helmet><title>{t('forgotPassword.title')} — Karma</title></Helmet>
      <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden">
        <DecorativeBlob className="w-96 h-96 -top-40 -right-40" variant="primary" />
        <div className="w-full max-w-md p-8 bg-card rounded-3xl shadow-lg relative z-10 mx-4 space-y-4">
          <div>
            <h1 className="font-heading text-3xl font-bold mb-2">{t('forgotPassword.title')}</h1>
            <p className="font-body text-muted-foreground">{t('forgotPassword.subtitle')}</p>
          </div>
          <form
            className="space-y-4"
            onSubmit={(event) => {
              event.preventDefault();
              mutation.mutate({ email });
            }}
          >
            <div>
              <Label htmlFor="email">{t('login.email')}</Label>
              <Input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />
            </div>
            <Button type="submit" className="w-full rounded-full">{t('forgotPassword.submit')}</Button>
          </form>
          {mutation.data ? (
            <div className="rounded-xl bg-muted p-4 text-sm">
              <p>{mutation.data.message}</p>
              {mutation.data.token ? (
                <p className="mt-2 break-all">
                  {t('forgotPassword.demoToken')}: <Link className="text-primary underline" to={`/reset-password?token=${mutation.data.token}`}>{mutation.data.token}</Link>
                </p>
              ) : null}
            </div>
          ) : null}
          {mutation.isError ? <p className="text-sm text-destructive">{(mutation.error as Error).message}</p> : null}
          <Link to="/login" className="text-sm text-primary hover:text-primary-hover">{t('forgotPassword.backToLogin')}</Link>
        </div>
      </div>
    </>
  );
};

export default ForgotPasswordPage;
