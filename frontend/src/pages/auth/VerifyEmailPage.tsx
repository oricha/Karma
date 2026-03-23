import { useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Helmet } from 'react-helmet-async';
import { useTranslation } from 'react-i18next';
import DecorativeBlob from '@/components/common/DecorativeBlob';
import { useAuthStore } from '@/lib/auth';
import { api } from '@/lib/api';

const VerifyEmailPage = () => {
  const { t } = useTranslation('auth');
  const [searchParams] = useSearchParams();
  const accessToken = useAuthStore((state) => state.accessToken);
  const refreshToken = useAuthStore((state) => state.refreshToken);
  const setSession = useAuthStore((state) => state.setSession);
  const token = searchParams.get('token') ?? '';
  const mutation = useMutation({
    mutationFn: api.verifyEmail,
    onSuccess: (response) => {
      if (accessToken && refreshToken) {
        setSession({ accessToken, refreshToken, user: response.user });
      }
    },
  });

  useEffect(() => {
    if (token) {
      mutation.mutate(token);
    }
  }, [mutation, token]);

  return (
    <>
      <Helmet><title>{t('verifyEmail.title')} — Karma</title></Helmet>
      <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden">
        <DecorativeBlob className="w-96 h-96 -top-40 -left-40" variant="secondary" />
        <div className="w-full max-w-md p-8 bg-card rounded-3xl shadow-lg relative z-10 mx-4 space-y-4">
          <div>
            <h1 className="font-heading text-3xl font-bold mb-2">{t('verifyEmail.title')}</h1>
            <p className="font-body text-muted-foreground">{t('verifyEmail.subtitle')}</p>
          </div>
          {!token ? <p className="text-sm text-destructive">{t('verifyEmail.missingToken')}</p> : null}
          {mutation.isPending ? <p className="text-sm">{t('verifyEmail.pending')}</p> : null}
          {mutation.data ? <p className="text-sm text-emerald-700">{mutation.data.message}</p> : null}
          {mutation.isError ? <p className="text-sm text-destructive">{(mutation.error as Error).message}</p> : null}
          <Link to="/account/details" className="text-sm text-primary hover:text-primary-hover">{t('verifyEmail.goToAccount')}</Link>
        </div>
      </div>
    </>
  );
};

export default VerifyEmailPage;
