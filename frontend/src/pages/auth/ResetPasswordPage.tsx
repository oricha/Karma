import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { useTranslation } from 'react-i18next';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { api } from '@/lib/api';

const ResetPasswordPage = () => {
  const { t } = useTranslation('auth');
  const [searchParams] = useSearchParams();
  const [token, setToken] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const mutation = useMutation({
    mutationFn: api.resetPassword,
  });

  useEffect(() => {
    setToken(searchParams.get('token') ?? '');
  }, [searchParams]);

  return (
    <>
      <Helmet><title>{t('resetPassword.title')} — Karma</title></Helmet>
      <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden">
        <DecorativeBlob className="w-72 h-72 -bottom-20 -left-20" variant="secondary" />
        <div className="w-full max-w-md p-8 bg-card rounded-3xl shadow-lg relative z-10 mx-4 space-y-4">
          <div>
            <h1 className="font-heading text-3xl font-bold mb-2">{t('resetPassword.title')}</h1>
            <p className="font-body text-muted-foreground">{t('resetPassword.subtitle')}</p>
          </div>
          <form
            className="space-y-4"
            onSubmit={(event) => {
              event.preventDefault();
              mutation.mutate({ token, password });
            }}
          >
            <div>
              <Label htmlFor="token">{t('resetPassword.token')}</Label>
              <Input id="token" value={token} onChange={(event) => setToken(event.target.value)} />
            </div>
            <div>
              <Label htmlFor="password">{t('register.password')}</Label>
              <Input id="password" type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
            </div>
            <div>
              <Label htmlFor="confirmPassword">{t('register.confirmPassword')}</Label>
              <Input id="confirmPassword" type="password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} />
            </div>
            <Button type="submit" className="w-full rounded-full" disabled={!token || password !== confirmPassword}>
              {t('resetPassword.submit')}
            </Button>
          </form>
          {password !== confirmPassword ? <p className="text-sm text-destructive">{t('resetPassword.mismatch')}</p> : null}
          {mutation.data ? <p className="text-sm text-emerald-700">{mutation.data.message}</p> : null}
          {mutation.isError ? <p className="text-sm text-destructive">{(mutation.error as Error).message}</p> : null}
          <Link to="/login" className="text-sm text-primary hover:text-primary-hover">{t('forgotPassword.backToLogin')}</Link>
        </div>
      </div>
    </>
  );
};

export default ResetPasswordPage;
