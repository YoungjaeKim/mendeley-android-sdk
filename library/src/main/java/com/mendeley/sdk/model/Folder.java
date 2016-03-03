package com.mendeley.sdk.model;

import java.util.Date;
import java.util.UUID;

/**
 * Model class representing folder json object.
 */
public class Folder {
	public final String name;
	public final UUID id;
	public final UUID parentId;
	public final UUID groupId;
	public final Date added;
	
	private Folder(String name, UUID id, UUID parentId, UUID groupId, Date added) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.groupId = groupId;
        this.added = added;
    }
	
	public static class Builder {
        private String name;
        private UUID id;
        private UUID parentId;
        private UUID groupId;
        private Date added;

        public Builder() {
        }

        public Builder(Folder folder) {
            this.name = folder.name;
            this.id = folder.id;
            this.parentId = folder.parentId;
            this.groupId = folder.groupId;
            this.added = folder.added;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setParentId(UUID parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder setGroupId(UUID groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder setAdded(Date added) {
            this.added = added;
            return this;
        }

        public Folder build() {
            return new Folder(
            		name,
                    id,
                    parentId,
                    groupId,
                    added
            );
        }
    }
	
	@Override
	public String toString() {
		return "name: " + name + 
			   ", parentId: " + parentId +
			   ", id: " + id + 
			   ", groupId: " + groupId + 
			   ", added: " + added;
	}
	
	@Override
	public boolean equals(Object object) {
		
		Folder other;
		
		try {
			other = (Folder) object;
		}
		catch (ClassCastException e) {
			return false;
		}
		
		if (other == null) {
			return false;
		} else {
			return other.id.equals(this.id);
		}
	}
}
