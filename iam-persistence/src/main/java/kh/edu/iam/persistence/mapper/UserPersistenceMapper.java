package kh.edu.iam.persistence.mapper;

import kh.edu.iam.Address;
import kh.edu.iam.User;
import kh.edu.iam.UserProfile;
import kh.edu.iam.persistence.entity.UserEntity;

public final class UserPersistenceMapper {
    private UserPersistenceMapper() {}

    public static void apply(User user, UserEntity entity) {
        entity.setUuid(user.getUuid());
        var profile = user.getProfile();
        var address = profile.address();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setEmailVerified(user.isEmailVerified());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setEnabled(user.isEnabled());
        entity.setAccountNonExpired(user.isAccountNonExpired());
        entity.setAccountNonLocked(user.isAccountNonLocked());
        entity.setCredentialsNonExpired(user.isCredentialsNonExpired());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLastLoginAt(user.getLastLoginAt());
        entity.setPasswordChangedAt(user.getPasswordChangedAt());
        entity.setGivenName(profile.givenName());
        entity.setFamilyName(profile.familyName());
        entity.setMiddleName(profile.middleName());
        entity.setNickname(profile.nickname());
        entity.setGender(profile.gender());
        entity.setBirthdate(profile.birthdate());
        entity.setProfileUrl(profile.profileUrl());
        entity.setPictureUrl(profile.pictureUrl());
        entity.setWebsiteUrl(profile.websiteUrl());
        entity.setZoneinfo(profile.zoneinfo());
        entity.setLocale(profile.locale());
        entity.setPhoneNumber(profile.phoneNumber());
        entity.setPhoneNumberVerified(profile.phoneNumberVerified());
        entity.setAddressStreetAddress(address == null ? null : address.streetAddress());
        entity.setAddressLocality(address == null ? null : address.locality());
        entity.setAddressRegion(address == null ? null : address.region());
        entity.setAddressPostalCode(address == null ? null : address.postalCode());
        entity.setAddressCountry(address == null ? null : address.country());
        AuditPersistenceMapper.apply(entity, user.getAudit(), user.getDeletion());
    }

    public static User toDomain(UserEntity entity) {
        Address address = null;
        if (entity.getAddressStreetAddress() != null
                || entity.getAddressLocality() != null
                || entity.getAddressRegion() != null
                || entity.getAddressPostalCode() != null
                || entity.getAddressCountry() != null) {
            address =
                    new Address(
                            entity.getAddressStreetAddress(),
                            entity.getAddressLocality(),
                            entity.getAddressRegion(),
                            entity.getAddressPostalCode(),
                            entity.getAddressCountry());
        }
        var profile =
                new UserProfile(
                        entity.getGivenName(),
                        entity.getFamilyName(),
                        entity.getMiddleName(),
                        entity.getNickname(),
                        entity.getGender(),
                        entity.getBirthdate(),
                        entity.getProfileUrl(),
                        entity.getPictureUrl(),
                        entity.getWebsiteUrl(),
                        entity.getZoneinfo(),
                        entity.getLocale(),
                        entity.getPhoneNumber(),
                        entity.isPhoneNumberVerified(),
                        address);
        return new User(
                entity.getId(),
                entity.getUuid(),
                entity.getUsername(),
                entity.getEmail(),
                entity.isEmailVerified(),
                entity.getPasswordHash(),
                profile,
                entity.isEnabled(),
                entity.isAccountNonExpired(),
                entity.isAccountNonLocked(),
                entity.isCredentialsNonExpired(),
                entity.getFailedLoginAttempts(),
                entity.getLastLoginAt(),
                entity.getPasswordChangedAt(),
                AuditPersistenceMapper.audit(entity),
                AuditPersistenceMapper.deletion(entity));
    }
}
