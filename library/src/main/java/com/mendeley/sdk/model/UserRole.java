package com.mendeley.sdk.model;

import java.util.UUID;

/**
 * Model class representing user role json object.
 *
 */
public class UserRole {

    public final UUID profileId;
    public final String joined;
    public final String role;

    public UserRole(
            UUID profileId,
            String joined,
            String role) {
        this.profileId = profileId;
        this.joined = joined;
        this.role = role;
    }

    public static class Builder {
        private UUID profileId;
        private String joined;
        private String role;

        public Builder() {}

        public Builder(UserRole from) {
            this.profileId = from.profileId;
            this.joined = from.joined;
            this.role = from.role;
        }

        public Builder setProfileId(UUID profileId) {
            this.profileId = profileId;
            return this;
        }

        public Builder setJoined(String joined) {
            this.joined = joined;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public UserRole build() {
            return new UserRole(
                    profileId,
                    joined,
                    role);
        }
    }
}
