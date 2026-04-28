package com.karma.platform.config;

import com.karma.platform.common.i18n.KarmaLocaleResolver;
import com.karma.platform.common.i18n.LocaleCookieInterceptor;
import com.karma.platform.seed.PlatformDataStore;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class I18nConfig {

    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    LocaleResolver localeResolver(PlatformDataStore dataStore, KarmaI18nProperties properties) {
        return new KarmaLocaleResolver(dataStore, properties);
    }

    @Bean
    LocaleCookieInterceptor localeCookieInterceptor(LocaleResolver localeResolver) {
        return new LocaleCookieInterceptor(localeResolver);
    }

    @Bean
    WebMvcConfigurer localeWebMvcConfigurer(LocaleCookieInterceptor localeCookieInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(localeCookieInterceptor);
            }
        };
    }
}
