package com.mendeley.sdk.model;

import java.util.UUID;

/**
 * Model class representing file json object.
 *
 */
public class File {

    public final UUID id;
    public final UUID documentId;
    public final String mimeType;
    public final String fileName;
    public final String fileHash;
    /** Size of file in bytes */
    public final int fileSize;

    private File(UUID id, UUID documentId, String mimeType, String fileName, String fileHash, int fileSize) {
        this.id = id;
        this.documentId = documentId;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "id: " + id +
                ", documentId: " + documentId +
                ", mimeType: " + mimeType +
                ", fileName: " + fileName +
                ", fileHash: " + fileHash +
                ", fileSize: " + fileSize ;

    }

    @Override
    public boolean equals(Object object) {
        File other;

        try {
            other = (File) object;
        } catch (ClassCastException e) {
            return false;
        }

        if (other == null) {
            return false;
        } else {
            return other.id.equals(this.id);
        }
    }

    public static class Builder {
        private UUID id;
        private UUID documentId;
        private String mimeType;
        private String fileName;
        private String fileHash;
        private int fileSize;

        public Builder() {}

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setDocumentId(UUID documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setFileHash(String fileHash) {
            this.fileHash = fileHash;
            return this;
        }

        public Builder setFileSize(int fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public File build() {
            return new File(
                    id,
                    documentId,
                    mimeType,
                    fileName,
                    fileHash,
                    fileSize
            );
        }
    }
}
