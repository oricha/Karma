package com.karma.platform.common.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalizationServiceTests {

    @Test
    void resolvesMessageForProvidedLocale() {
        StaticMessageSource source = new StaticMessageSource();
        source.addMessage("error.user-not-found", Locale.ENGLISH, "User not found");
        source.addMessage("error.user-not-found", Locale.forLanguageTag("es"), "Usuario no encontrado");

        LocalizationService service = new LocalizationService(source);

        assertEquals("Usuario no encontrado", service.message("error.user-not-found", Locale.forLanguageTag("es")));
        assertEquals("User not found", service.message("error.user-not-found", Locale.ENGLISH));
    }
}
