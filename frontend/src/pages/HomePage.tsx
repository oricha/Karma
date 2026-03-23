import { Helmet } from 'react-helmet-async';
import HeroSection from '@/components/home/HeroSection';
import PopularEvents from '@/components/home/PopularEvents';
import FeaturedGroups from '@/components/home/FeaturedGroups';
import ExploreThemes from '@/components/home/ExploreThemes';
import CommunitySection from '@/components/home/CommunitySection';
import WhyKarma from '@/components/home/WhyKarma';
import NewsletterSignup from '@/components/home/NewsletterSignup';

const HomePage = () => {
  return (
    <>
      <Helmet>
        <title>Karma — Nutre el Cuerpo, Despierta el Alma</title>
        <meta name="description" content="Descubre eventos de bienestar, salud y espiritualidad. Únete a comunidades de yoga, danza, meditación y más." />
      </Helmet>
      <HeroSection />
      <PopularEvents />
      <FeaturedGroups />
      <ExploreThemes />
      <CommunitySection />
      <WhyKarma />
      <NewsletterSignup />
    </>
  );
};

export default HomePage;
