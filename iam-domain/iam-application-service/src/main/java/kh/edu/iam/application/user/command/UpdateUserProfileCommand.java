package kh.edu.iam.application.user.command;

import kh.edu.iam.Address;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateUserProfileCommand(UUID userUuid, ProfilePatch patch, Long expectedVersion) {
    public UpdateUserProfileCommand(UUID userUuid, ProfilePatch patch) {
        this(userUuid, patch, null);
    }

    public UpdateUserProfileCommand(
            UUID uuid,
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
            Address address) {
        this(
                uuid,
                legacy(
                        givenName,
                        familyName,
                        middleName,
                        nickname,
                        gender,
                        birthdate,
                        profileUrl,
                        pictureUrl,
                        websiteUrl,
                        zoneinfo,
                        locale,
                        phoneNumber,
                        address),
                null);
    }

    private static ProfilePatch legacy(
            String given,
            String family,
            String middle,
            String nickname,
            String gender,
            LocalDate birth,
            String profile,
            String picture,
            String website,
            String zone,
            String locale,
            String phone,
            Address address) {
        var b = ProfilePatch.builder();
        if (given != null) {
            b.givenName(given);
        }
        if (family != null) {
            b.familyName(family);
        }
        if (middle != null) {
            b.middleName(middle);
        }
        if (nickname != null) {
            b.nickname(nickname);
        }
        if (gender != null) {
            b.gender(gender);
        }
        if (birth != null) {
            b.birthdate(birth);
        }
        if (profile != null) {
            b.profileUrl(profile);
        }
        if (picture != null) {
            b.pictureUrl(picture);
        }
        if (website != null) {
            b.websiteUrl(website);
        }
        if (zone != null) {
            b.zoneinfo(zone);
        }
        if (locale != null) {
            b.locale(locale);
        }
        if (phone != null) {
            b.phoneNumber(phone);
        }
        if (address != null) {
            b.address(address);
        }
        return b.build();
    }
}
