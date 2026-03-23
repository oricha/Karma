import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQuery } from '@tanstack/react-query';
import AccountLayout from '@/components/layout/AccountLayout';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { Input } from '@/components/ui/input';
import { Slider } from '@/components/ui/slider';
import ThemePill from '@/components/categories/ThemePill';
import { api, type UserPreferences } from '@/lib/api';
import type { Category } from '@/types';

const newsletterOptions = ['weekly', 'biweekly', 'monthly', 'karmaOnly', 'never'] as const;
type NewsletterOption = typeof newsletterOptions[number];

const PreferencesPage = () => {
  const { t } = useTranslation('account');
  const [newsletterFreq, setNewsletterFreq] = useState<NewsletterOption>('weekly');
  const [selectedThemes, setSelectedThemes] = useState<string[]>([]);
  const [radius, setRadius] = useState([50]);
  const [reviewReminders, setReviewReminders] = useState(true);
  const [location, setLocation] = useState('Madrid');
  const { data: preferences } = useQuery({ queryKey: ['preferences'], queryFn: api.getPreferences });
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: api.getCategories });
  const { data: themeOptions = [] } = useQuery({
    queryKey: ['category-themes', categories.map((category) => category.slug).join(',')],
    enabled: categories.length > 0,
    queryFn: async () => {
      const responses = await Promise.all(categories.map((category: Category) => api.getCategoryDetails(category.slug)));
      return responses.flatMap((response) => response.themes);
    },
  });
  const mutation = useMutation({ mutationFn: api.updatePreferences });

  useEffect(() => {
    if (preferences) {
      setNewsletterFreq(fromApiFrequency(preferences.newsletterFrequency));
      setRadius([preferences.locationRadiusKm]);
      setReviewReminders(preferences.reviewReminders);
      setLocation(preferences.preferredLocation);
      setSelectedThemes(preferences.themeIds);
    }
  }, [preferences]);

  const toggleTheme = (theme: string) => {
    setSelectedThemes(prev =>
      prev.includes(theme) ? prev.filter(t => t !== theme) : [...prev, theme]
    );
  };

  return (
    <AccountLayout>
      <h1 className="font-heading text-2xl font-bold mb-6">{t('preferences.title')}</h1>

      <div className="space-y-8 max-w-lg">
        {/* Newsletter frequency */}
        <div>
          <h3 className="font-heading text-lg font-semibold mb-3">{t('preferences.newsletter')}</h3>
          <div className="space-y-2">
            {newsletterOptions.map(opt => (
              <button
                key={opt}
                onClick={() => setNewsletterFreq(opt)}
                className={`block w-full text-left px-4 py-3 rounded-xl font-body text-sm transition-colors ${
                  newsletterFreq === opt
                    ? 'bg-primary text-primary-foreground'
                    : 'bg-card hover:bg-muted'
                }`}
              >
                {t(`preferences.${opt}`)}
              </button>
            ))}
          </div>
        </div>

        {/* Reviews toggle */}
        <div className="flex items-center justify-between bg-card rounded-xl p-4">
          <span className="font-body text-sm">{t('preferences.reviews')}</span>
          <Switch checked={reviewReminders} onCheckedChange={setReviewReminders} />
        </div>

        {/* Theme preferences */}
        <div>
          <h3 className="font-heading text-lg font-semibold mb-3">{t('preferences.themes')}</h3>
          <div className="flex flex-wrap gap-2">
            {themeOptions.map(theme => (
              <ThemePill
                key={theme.id}
                label={theme.nameEs}
                active={selectedThemes.includes(theme.id)}
                onClick={() => toggleTheme(theme.id)}
              />
            ))}
          </div>
        </div>

        {/* Location */}
        <div>
          <h3 className="font-heading text-lg font-semibold mb-3">{t('preferences.location')}</h3>
          <Input placeholder="Madrid" className="rounded-lg font-body mb-4" value={location} onChange={(e) => setLocation(e.target.value)} />
          <div>
            <div className="flex justify-between text-sm font-body text-muted-foreground mb-2">
              <span>{t('preferences.radius')}</span>
              <span>{radius[0]} km</span>
            </div>
            <Slider value={radius} onValueChange={setRadius} min={10} max={100} step={5} />
          </div>
        </div>

        <Button
          className="rounded-full font-body"
          onClick={() => mutation.mutate({
            newsletterFrequency: toApiFrequency(newsletterFreq),
            reviewReminders,
            preferredLocation: location,
            latitude: 40.4168,
            longitude: -3.7038,
            locationRadiusKm: radius[0],
            themeIds: selectedThemes,
          })}
        >
          {t('common:buttons.saveChanges')}
        </Button>
      </div>
    </AccountLayout>
  );
};

const toApiFrequency = (value: NewsletterOption): UserPreferences['newsletterFrequency'] => {
  switch (value) {
    case 'weekly':
      return 'WEEKLY';
    case 'biweekly':
      return 'BIWEEKLY';
    case 'monthly':
      return 'MONTHLY';
    case 'karmaOnly':
      return 'KARMA_ONLY';
    case 'never':
      return 'NEVER';
  }
};

const fromApiFrequency = (value: UserPreferences['newsletterFrequency']): NewsletterOption => {
  switch (value) {
    case 'WEEKLY':
      return 'weekly';
    case 'BIWEEKLY':
      return 'biweekly';
    case 'MONTHLY':
      return 'monthly';
    case 'KARMA_ONLY':
      return 'karmaOnly';
    case 'NEVER':
      return 'never';
  }
};

export default PreferencesPage;
