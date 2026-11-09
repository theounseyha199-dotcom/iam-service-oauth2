package kh.edu.iam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.Set;

public record UserProfile(
        String givenName,
        String familyName,
        String middleName,
        String nickname,
        String gender,
        LocalDate birthdate,
        String profileUrl,
        String pictureUrl,
        String websiteUrl,
        String zoneinfo,
        String locale,
        String phoneNumber,
        boolean phoneNumberVerified,
        Address address
) {

    public UserProfile {
        givenName = required(givenName, 100, "givenName");
        familyName = required(familyName, 100, "familyName");

        middleName = optional(middleName, 100, "middleName");
        nickname = optional(nickname, 100, "nickname");
        gender = optional(gender, 20, "gender");

        profileUrl = optional(profileUrl, 500, "profileUrl");
        pictureUrl = optional(pictureUrl, 500, "pictureUrl");
        websiteUrl = optional(websiteUrl, 500, "websiteUrl");
        zoneinfo = optional(zoneinfo, 50, "zoneinfo");
        phoneNumber = optional(phoneNumber, 20, "phoneNumber");

        if (gender != null
                && !Set.of("female", "male", "other").contains(gender)) {
            throw new IllegalArgumentException(
                    "gender must be female, male or other"
            );
        }

        if (zoneinfo != null) {
            try {
                ZoneId.of(zoneinfo);
            } catch (DateTimeException exception) {
                throw new IllegalArgumentException(
                        "zoneinfo must be a valid time zone",
                        exception
                );
            }
        }

        if (locale == null || !Set.of("en", "km").contains(locale)) {
            throw new IllegalArgumentException(
                    "locale must be en or km"
            );
        }

        if (phoneNumber != null
                && !phoneNumber.matches("\\+[1-9][0-9]{1,14}")) {
            throw new IllegalArgumentException(
                    "phoneNumber must use E.164 format"
            );
        }

        if (phoneNumberVerified && phoneNumber == null) {
            throw new IllegalArgumentException(
                    "A verified phone number is required"
            );
        }
    }

    public static UserProfile basic(
            String givenName,
            String familyName,
            String locale
    ) {
        return new UserProfile(
                givenName,
                familyName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                locale,
                null,
                false,
                null
        );
    }

    public String name() {
        return givenName + " " + familyName;
    }

    private static String required(
            String value,
            int maxLength,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }

        return optional(value, maxLength, field);
    }

    private static String optional(
            String value,
            int maxLength,
            String field
    ) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " cannot exceed "
                            + maxLength + " characters"
            );
        }

        return value;
    }}