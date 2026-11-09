package kh.edu.iam;

import java.util.Locale;

public record Address(String streetAddress, String locality, String region, String postalCode, String country) {

    public Address {
        streetAddress = validateLength(streetAddress, 255, "streetAddress");
        locality = validateLength(locality, 100, "locality");
        region = validateLength(region, 100, "region");
        postalCode = validateLength(postalCode, 20, "postalCode");

        if (country != null) {
            country = country.strip().toUpperCase(Locale.ROOT);

            if (!Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).contains(country)) {
                throw new IllegalArgumentException("country must be a valid ISO 3166-1 alpha-2 code");
            }
        }
    }

    private static String validateLength(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(field + " cannot exceed " + maxLength + " characters");
        }

        return value;
    }
}
