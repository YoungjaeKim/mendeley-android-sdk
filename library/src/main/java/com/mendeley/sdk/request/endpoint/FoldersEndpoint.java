package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PatchAuthorizedRequest;
import com.mendeley.sdk.request.PostAuthorizedRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /folders endpoint.
 * {@see http://dev.mendeley.com/methods/#folders}
 */
public class FoldersEndpoint {

	public static final String FOLDERS_BASE_URL = MENDELEY_API_BASE_URL + "folders";
    public static final String FOLDER_CONTENT_TYPE = "application/vnd.mendeley-folder.1+json";

    public static class GetFoldersRequest extends GetAuthorizedRequest<List<Folder>> {
        private static Uri getGetFoldersUrl(FolderRequestParameters params) {
            Uri uri = Uri.parse(FOLDERS_BASE_URL);
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetFoldersRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetFoldersRequest(FolderRequestParameters parameters, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(getGetFoldersUrl(parameters), authTokenManager, appCredentials);
        }

        @Override
        protected List<Folder> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.foldersFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }
    }

    public static class GetFolderRequest extends GetAuthorizedRequest<Folder> {
        public GetFolderRequest(UUID folderId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL + "/" + folderId), authTokenManager, appCredentials);
        }

        @Override
        protected Folder manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.folderFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", FOLDER_CONTENT_TYPE);
        }
    }

    public static class PostFolderRequest extends PostAuthorizedRequest<Folder> {
        private final Folder folder;

        public PostFolderRequest(Folder folder, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FoldersEndpoint.FOLDERS_BASE_URL), authTokenManager, appCredentials);
            this.folder = folder;
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(FOLDER_CONTENT_TYPE), JsonParser.folderToJson(folder).toString());
        }

        @Override
        protected Folder manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.folderFromJson(reader);
        }
    }

    public static class PatchFolderAuthorizedRequest extends PatchAuthorizedRequest<Folder> {
        private final Folder folder;

        public PatchFolderAuthorizedRequest(UUID folderId, Folder folder, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL + "/" + folderId), null, authTokenManager, appCredentials);
            this.folder = folder;
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(FOLDER_CONTENT_TYPE), JsonParser.folderToJson(folder).toString());
        }

        @Override
        protected Folder manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.folderFromJson(reader);
        }
    }

    public static class PostDocumentToFolderRequest extends PostAuthorizedRequest<Void> {
        private final UUID documentId;

        public PostDocumentToFolderRequest(UUID folderId, UUID documentId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId.toString()).appendPath("documents").build(), authTokenManager, appCredentials);
            this.documentId = documentId;
        }

        @Override
        protected Void manageResponse(InputStream is) throws Exception {
            return null;
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(DocumentEndpoint.DOCUMENTS_CONTENT_TYPE), JsonParser.documentIdToJson(documentId).toString());
        }
    }

    public static class GetFolderDocumentIdsRequest extends GetAuthorizedRequest<List<UUID>> {

        private static Uri getGetFolderDocumentIdsUrl(FolderRequestParameters params, UUID folderId) {
            Uri uri = Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId.toString()).appendPath("documents").build();
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetFolderDocumentIdsRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetFolderDocumentIdsRequest(FolderRequestParameters parameters, UUID folderId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this(getGetFolderDocumentIdsUrl(parameters, folderId), authTokenManager, appCredentials);
        }

        @Override
        protected List<UUID> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.documentsIdsFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", DocumentEndpoint.DOCUMENTS_CONTENT_TYPE);
        }
    }

    public static class DeleteFolderRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteFolderRequest(UUID folderId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId.toString()).build(), authTokenManager, appCredentials);
        }
    }

    public static class DeleteDocumentFromFolder extends DeleteAuthorizedRequest<Void> {
        public DeleteDocumentFromFolder(UUID folderId, UUID documentId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(FOLDERS_BASE_URL).buildUpon().appendPath(folderId.toString()).appendPath("documents").appendPath(documentId.toString()).build(), authTokenManager, appCredentials);
        }
    }

    /**
     * This class represents parameters for folder SDK requests.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class FolderRequestParameters {
        /**
         * Group ID. If not supplied, returns user folders.
         */
        public UUID groupId;

        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;


        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();

            if (groupId != null) {
                bld.appendQueryParameter("group_id", groupId.toString());
            }
            if (limit != null) {
                bld.appendQueryParameter("limit", Integer.toString(limit));
            }

            return bld.build();
        }
    }
}
