package com.karma.platform.common.geocoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAddressFormatterTests {

    private final DefaultAddressFormatter formatter = new DefaultAddressFormatter();

    @Test
    void formatsAddressWithoutBlankSegments() {
        assertEquals("Espacio Gaia, Calle del Sol 12, Madrid, Espana",
                formatter.format("Espacio Gaia", "Calle del Sol 12", "Madrid", "Espana"));
    }

    @Test
    void normalizesAccentsAndSymbols() {
        assertEquals("circulo de cacao valencia, espana", formatter.normalize("Círculo de Cacao - Valencia, España"));
    }
}
