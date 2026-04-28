package com.karma.platform.common.geocoding;

public interface AddressFormatter {

    String format(String... parts);

    String normalize(String value);
}
