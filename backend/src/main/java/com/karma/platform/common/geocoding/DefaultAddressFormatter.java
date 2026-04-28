package com.karma.platform.common.geocoding;

import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class DefaultAddressFormatter implements AddressFormatter {

    @Override
    public String format(String... parts) {
        return Arrays.stream(parts)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9, ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
