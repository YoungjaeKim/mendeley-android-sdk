package com.mendeley.sdk.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing Group json object.
 *
 */
public class Group {

    /** Different access levels for a group */
    public static enum AccessLevel {
        PUBLIC("public"),
        PRIVATE("private"),
        INVITE_ONLY("invite_only");

        private final String stringValue;

        private AccessLevel(String stringValue) {
            this.stringValue = stringValue;
        }

        public static AccessLevel fromValue(String accessLevelStr) {
            for(AccessLevel level : AccessLevel.values()) {
                if (level.toValue().equals(accessLevelStr)) {
                    return level;
                }
            }
            throw new IllegalArgumentException("Invalid access level: " + accessLevelStr);
        }

        public String toValue() {
            return stringValue;
        }
    }

    /** Different roles for a group */
    public static enum Role {
        OWNER("owner"),
        ADMIN("admin"),
        NORMAL("normal"),
        INVITED("invited"),
        FOLLOWER("follower");

        private final String stringValue;

        private Role(String stringValue) {
            this.stringValue = stringValue;
        }

        public static Role fromValue(String roleStr) {
            for(Role role : Role.values()) {
                if (role.toValue().equals(roleStr)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }

        public String toValue() {
            return stringValue;
        }
    }

    public final UUID id;
    public final Date created;
    public final UUID owningProfileId;
    public final String link;
    public final Role role;
    public final AccessLevel accessLevel;
    public final String name;
    public final String description;
    public final List<String> tags;
    public final String webpage;
    public final List<String> disciplines;
    public final Photo photo;

    private Group(
            UUID id,
            Date created,
            UUID owningProfileId,
            String link,
            Role role,
            AccessLevel accessLevel,
            String name,
            String description,
            List<String> tags,
            String webpage,
            List<String> disciplines,
            Photo photo) {
        this.id = id;
        this.created = created;
        this.owningProfileId = owningProfileId;
        this.link = link;
        this.role = role;
        this.accessLevel = accessLevel;
        this.name = name;
        this.description = description;
        this.tags = tags;
        this.webpage = webpage;
        this.disciplines = disciplines;
        this.photo = photo;
    }

    public static class Builder {
            private UUID id;
            private Date created;
            private UUID owningProfileId;
            private String link;
            private Role role;
            private AccessLevel accessLevel;
            private String name;
            private String description;
            private List<String> tags;
            private String webpage;
            private List<String> disciplines;
            private Photo photo;

        public Builder() {}

        public Builder(Group from) {
            this.id = from.id;
            this.created = from.created;
            this.owningProfileId = from.owningProfileId;
            this.link = from.link;
            this.role = from.role;
            this.accessLevel = from.accessLevel;
            this.name = from.name;
            this.description = from.description;
            this.tags = from.tags==null?new ArrayList<String>():from.tags;
            this.webpage = from.webpage;
            this.disciplines = from.disciplines==null?new ArrayList<String>():from.disciplines;
            this.photo = from.photo;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setCreated(Date created) {
            this.created = created;
            return this;
        }

        public Builder setOwningProfileId(UUID owingProfileId) {
            this.owningProfileId = owingProfileId;
            return this;
        }

        public Builder setLink(String link) {
            this.link = link;
            return this;
        }

        public Builder setRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder setAccessLevel(AccessLevel accessLevel) {
            this.accessLevel = accessLevel;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder setWebpage(String webpage) {
            this.webpage = webpage;
            return this;
        }

        public Builder setDisciplines(List<String> disciplines) {
            this.disciplines = disciplines;
            return this;
        }

        public Builder setPhoto(Photo photo) {
            this.photo = photo;
            return this;
        }

        public Group build() {
            return new Group(
                    id,
                    created,
                    owningProfileId,
                    link,
                    role,
                    accessLevel,
                    name,
                    description,
                    tags,
                    webpage,
                    disciplines,
                    photo);
        }
    }
}
