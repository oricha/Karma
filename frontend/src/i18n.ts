import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import commonEs from './locales/es/common.json';
import homeEs from './locales/es/home.json';
import eventsEs from './locales/es/events.json';
import groupsEs from './locales/es/groups.json';
import authEs from './locales/es/auth.json';
import accountEs from './locales/es/account.json';

import commonEn from './locales/en/common.json';
import homeEn from './locales/en/home.json';
import eventsEn from './locales/en/events.json';
import groupsEn from './locales/en/groups.json';
import authEn from './locales/en/auth.json';
import accountEn from './locales/en/account.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      es: {
        common: commonEs,
        home: homeEs,
        events: eventsEs,
        groups: groupsEs,
        auth: authEs,
        account: accountEs,
      },
      en: {
        common: commonEn,
        home: homeEn,
        events: eventsEn,
        groups: groupsEn,
        auth: authEn,
        account: accountEn,
      },
    },
    fallbackLng: 'es',
    defaultNS: 'common',
    interpolation: {
      escapeValue: false,
    },
    detection: {
      order: ['localStorage', 'navigator'],
      caches: ['localStorage'],
    },
  });

export default i18n;
