package com.mendeley.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mendeley.sdk.activity.SignInActivity;
import com.mendeley.sdk.activity.SignInOrSignUpActivity;
import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.model.UserRole;
import com.mendeley.sdk.request.endpoint.AnnotationsEndpoint;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;
import com.mendeley.sdk.request.endpoint.DocumentIdentifiersEndpoint;
import com.mendeley.sdk.request.endpoint.DocumentTypesEndpoint;
import com.mendeley.sdk.request.endpoint.FilesEndpoint;
import com.mendeley.sdk.request.endpoint.FoldersEndpoint;
import com.mendeley.sdk.request.endpoint.GroupsEndpoint;
import com.mendeley.sdk.request.endpoint.ProfilesEndpoint;
import com.mendeley.sdk.request.endpoint.RecentlyReadEndpoint;
import com.mendeley.sdk.request.endpoint.TrashEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class exposing all the pubic functionality of the Mendeley SDK.
 *
 * <p/>
 *
 * This class provides default implementations of {@link AuthTokenManager}, used to get
 * a valid authorization token and {@link RequestsFactory}, used to create HTTP {@link Request}s
 * against the Mendeley API.
 *
 * <p/>
 * This class can't be instantiated by client code and should always be accessed in a
 * singleton way using {@link Mendeley#getInstance()}
 *
 * <p/>
 * First thing client code should do is initialising this class by passing a valid
 * API key and credentials calling  {@link Mendeley#init(Context, String, String)}
 *
 * <p/>
 *
 * The Mendeley API is documented in the Developer portal of Mendeley @{see http://dev.mendeley.com/}
 *
 * <p/>
 *
 * Note: developers that wants to get rid of the {@link Mendeley} singleton for better dependency
 * injection and unit testing may ignore this class and directly instantiate a {@link AuthTokenManager}
 * and {@link RequestsFactory} if they want to do so.
 */
public class Mendeley {

    private static final String TAG = Mendeley.class.getSimpleName();
    private static Mendeley instance;

    private AppCredentials appCredentials;
    private AuthTokenManager authTokenManager;
    private RequestsFactory requestsFactory;

    /**
     * @return a reference to the @{Mendeley} SDK singleton.
     */
    public static synchronized Mendeley getInstance() {
        if (instance == null) {
            instance = new Mendeley();
        }
        return instance;
    }

    private Mendeley() {
    }

    /**
     * Initialises the SDK, providing a valid API key and credentials to obtain authorization tokens
     * from the Mendeley API
     *
     * @param context a Context, won't be kept as a reference.
     * @param appId, valid client app id
     * @param appSecret, valid client app secret
     */
    public final void init(Context context, String appId, String appSecret) {
        this.appCredentials = new AppCredentials(appId, appSecret);
        this.authTokenManager = new SharedPreferencesAuthTokenManager(context.getSharedPreferences(SharedPreferencesAuthTokenManager.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE));
        this.requestsFactory = new RequestFactoryImpl(authTokenManager, appCredentials);
    }

    /**
     * Signs the user in.
     *
     * <p/>
     *
     * Calling this method will open one {@link Activity} showing the UI that lets the user enter
     * their credentials to sign in into the Mendeley server.
     *
     * The SDK will take control of the flow, and will invoke @{Activity#onActivityResult} once the
     * sign in flow has been resolved.
     *
     * Your @{Activity} will need to pass the result back to this class invoking @{Mendeley#onActivityResult}
     * providing a {@link com.mendeley.sdk.Mendeley.SignInCallback} that can be used by your
     * code to get the final outcome of the signing in attempt.
     *
     * @param activity used for creating the sign in @{Activity}. The SDK won't hold any reference to this,
     *                 so there is no risk of leaking it.
     * @param showSignUpScreen whether or not to show the screen with UI to create a new account before the sign-in screen
     */
    public final void signIn(Activity activity, boolean showSignUpScreen) {
        assertInitialised();

        final Class<? extends Activity> activityClass = showSignUpScreen ? SignInOrSignUpActivity.class : SignInActivity.class;
        final Intent intent = new Intent(activity, activityClass);
        activity.startActivityForResult(intent, SignInActivity.AUTH_REQUEST_CODE);
    }


    /**
     * Signs the user out.
     *
     * <p/>
     *
     * In practice, this simply means clearing the authorization tokens from the Mendeley SDK, if any.*
     */
    public void signOut() {
        assertInitialised();
        authTokenManager.clearTokens();
    }

    /**
     * To be called from the {@link Activity#onActivityResult(int, int, Intent)} of your application.
     *
     * <p/>
     *
     * Once the user has signed in to the Mendeley server, the Activity that performs that work will
     * pass the result using {@link Activity#onActivityResult(int, int, Intent)}. Hence, you need
     * to pass the result back to the SDK by calling this method in your Activity, so that the SDK
     * will analyse the result and persist the authorization token.
     *
     *
     * @param requestCode the requestCode received in the onActivityResult of your app
     * @param resultCode the resultCode received in the onActivityResult of your app
     * @param data the data received in the onActivityResult of your app
     * @param signInCallback a callback letting client code know the outcome of the sign in attempt
     *
     * @return true if the result relates to a sign in attempt and has been consumed by the Mendeley SDK
     */
    public final boolean onActivityResult(int requestCode, int resultCode, Intent data, SignInCallback signInCallback) {
        assertInitialised();

        switch (requestCode) {
            case SignInActivity.AUTH_REQUEST_CODE:
                onLoginActivityResult(resultCode, data, signInCallback);
                return true;
            default:
                return false;
        }
    }

    private void onLoginActivityResult(int resultCode, Intent data, SignInCallback signInCallback) {
        if (resultCode == Activity.RESULT_OK && onJsonStringResult(data.getStringExtra(SignInActivity.EXTRA_JSON_TOKENS))) {
            signInCallback.onSignedIn();
        } else {
            signInCallback.onSignInFailure();
        }
    }

    private boolean onJsonStringResult(String jsonTokenString) {
        try {
            JSONObject tokenObject = new JSONObject(jsonTokenString);

            String accessToken = tokenObject.getString("access_token");
            String refreshToken = tokenObject.getString("refresh_token");
            String tokenType = tokenObject.getString("token_type");
            int expiresIn = tokenObject.getInt("expires_in");

            authTokenManager.saveTokens(accessToken, refreshToken, tokenType, expiresIn);
            return true;
        } catch (JSONException e) {
            // If the client credentials are incorrect, the tokenString contains an error message
            Log.e(TAG, "Could not parse the json response with the auth tokens: " + jsonTokenString, e);
            return false;
        }
    }

    /**
     * Returns whether or not the user is signed in to the Mendeley API server.
     * This is, if the SDK has the needed information (OAuth access or refresh tokens) to launch
     * HTTP {@link Request}s against the Mendeley API.
     *
     * @return if the user is signed in
     */
    public final boolean isSignedIn() {
        assertInitialised();

        return !TextUtils.isEmpty(authTokenManager.getAccessToken());
    }

    /**
     * Returns one @{RequestsFactory} that you can use for creating typical {@link Request}s against
     * the Mendeley SDK.
     *
     * @return a Request factory to create typical Requests
     */
    public RequestsFactory getRequestFactory() {return requestsFactory; }

    /**
     * @return clients credentials used by this Mendeley SDK, as passed by the app
     * in the {@link Mendeley#init(Context, String, String)} method
     */
    public AppCredentials getAppCredentials() {
        return appCredentials;
    }

    /**
     * @return AuthTokenManager used by this Mendeley SDK
     */
    public AuthTokenManager getAuthTokenManager() {
        return authTokenManager;
    }

    private void assertInitialised() {
        if (authTokenManager == null) {
            throw new IllegalStateException("Sdk is not initialised. You must call #sdkInitialise() first.");
        }
    }

    /**
     * Interface that should be implemented by the application for receiving callbacks for sign
     * in events.
     */
    public interface SignInCallback {
        void onSignedIn();
        void onSignInFailure();
    }

    /**
     * Default implementation of {@link RequestsFactory}
     *
     * <p/>
     *
     * Typical Android applications using the Mendeley SDK won't directly need to deal with this,
     * but this class is left public in case you don't want to use the {@link Mendeley} singleton
     * in your app and you prefer to instantiate the {@link RequestsFactory} by yourself.
     */
    public static class RequestFactoryImpl implements RequestsFactory {

        private final AppCredentials appCredentials;
        private final AuthTokenManager authTokenManager;

        public RequestFactoryImpl(AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this.authTokenManager = authTokenManager;
            this.appCredentials = appCredentials;
        }

        @Override
        public Request<Profile> newGetMyProfileRequest() {
            return new ProfilesEndpoint.GetProfileRequest(authTokenManager, appCredentials);
        }

        @Override
        public Request<Profile> newGetProfileRequest(final UUID profileId) {
            return new ProfilesEndpoint.GetProfileRequest(profileId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentTypesRequest()  {
            return new DocumentTypesEndpoint.GetDocumentTypesRequest(authTokenManager, appCredentials);
        }

        @Override
        public Request<Map<String, String>> newGetDocumentIdentifierTypesRequest() {
            return new DocumentIdentifiersEndpoint.GetDocumentIdentifiersRequest(authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Document>> newGetDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters) {
            return new DocumentEndpoint.GetDocumentsRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Document>> newGetDocumentsRequest(Uri url) {
            return new DocumentEndpoint.GetDocumentsRequest(url, authTokenManager, appCredentials);
        }

        @Override
        public Request<Document> newGetDocumentRequest(UUID documentId, DocumentEndpoint.DocumentRequestParameters.View view) {
            return new DocumentEndpoint.GetDocumentRequest(documentId, view, authTokenManager, appCredentials);
        }

        @Override
        public Request<Document> newPostDocumentRequest(Document document) {
            return new DocumentEndpoint.PostDocumentRequest(document, authTokenManager, appCredentials);
        }

        @Override
        public Request<Document> newPatchDocumentRequest(UUID documentId, Date date, Document document) {
            return new DocumentEndpoint.PatchDocumentAuthorizedRequest(documentId, document, date, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newTrashDocumentRequest(UUID documentId) {
            return new DocumentEndpoint.TrashDocumentRequest(documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newDeleteDocumentRequest(UUID documentId) {
            return new DocumentEndpoint.DeleteDocumentRequest(documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newDeleteTrashedDocumentRequest(UUID documentId) {
            return new TrashEndpoint.DeleteTrashedDocumentRequest(documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Document>> newGetTrashedDocumentsRequest(DocumentEndpoint.DocumentRequestParameters parameters) {
            return new TrashEndpoint.GetTrashedDocumentsRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Document>> newGetTrashedDocumentsRequest(Uri uri) {
            return new TrashEndpoint.GetTrashedDocumentsRequest(uri, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newRestoreTrashedDocumentRequest(UUID documentId) {
            return new TrashEndpoint.RestoreTrashedDocumentRequest(documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Annotation>> newGetAnnotationsRequest(AnnotationsEndpoint.AnnotationRequestParameters parameters) {
            return new AnnotationsEndpoint.GetAnnotationsRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Annotation>> newGetAnnotationsRequest(Uri url) {
            return new AnnotationsEndpoint.GetAnnotationsRequest(url, authTokenManager, appCredentials);
        }

        @Override
        public Request<Annotation> newGetAnnotationRequest(UUID annotationId) {
            return new AnnotationsEndpoint.GetAnnotationRequest(annotationId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Annotation> newPostAnnotationRequest(Annotation annotation) {
            return new AnnotationsEndpoint.PostAnnotationRequest(annotation, authTokenManager, appCredentials);
        }

        @Override
        public Request<Annotation> newPatchAnnotationRequest(UUID annotationId, Annotation annotation) {
            return new AnnotationsEndpoint.PatchAnnotationRequest(annotationId, annotation, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newDeleteAnnotationRequest(UUID annotationId) {
            return new AnnotationsEndpoint.DeleteAnnotationRequest(annotationId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<File>> newGetFilesRequest(FilesEndpoint.FileRequestParameters parameters) {
            return new FilesEndpoint.GetFilesRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<File>> newGetFilesRequest(Uri uri) {
            return new FilesEndpoint.GetFilesRequest(uri, authTokenManager, appCredentials);
        }

        @Override
        public FilesEndpoint.GetFileBinaryRequest newGetFileBinaryRequest(UUID fileId, java.io.File targetFile) {
            return new FilesEndpoint.GetFileBinaryRequest(fileId, targetFile, authTokenManager, appCredentials);
        }

        @Override
        public Request<File> newPostFileWithBinaryRequest(String contentType, UUID documentId, InputStream inputStream, String fileName) {
            return new FilesEndpoint.PostFileWithBinaryRequest(contentType, documentId, fileName, inputStream, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newDeleteFileRequest(UUID fileId) {
            return new FilesEndpoint.DeleteFileRequest(fileId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Folder>> newGetFoldersRequest(FoldersEndpoint.FolderRequestParameters parameters) {
            return new FoldersEndpoint.GetFoldersRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Folder>> newGetFoldersRequest(Uri uri) {
            return new FoldersEndpoint.GetFoldersRequest(uri, authTokenManager, appCredentials);
        }

        @Override
        public Request<Folder> newGetFolderRequest(UUID folderId) {
            return new FoldersEndpoint.GetFolderRequest(folderId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Folder> newPostFolderRequest(Folder folder) {
            return new FoldersEndpoint.PostFolderRequest(folder, authTokenManager, appCredentials);
        }

        @Override
        public Request<Folder> newPatchFolderRequest(UUID folderId, Folder folder) {
            return new FoldersEndpoint.PatchFolderAuthorizedRequest(folderId, folder, authTokenManager, appCredentials);
        }


        @Override
        public Request<Void> newDeleteFolderRequest(UUID folderId) {
            return new FoldersEndpoint.DeleteFolderRequest(folderId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<UUID>> newGetFolderDocumentsRequest(FoldersEndpoint.FolderRequestParameters parameters, UUID folderId) {
            return new FoldersEndpoint.GetFolderDocumentIdsRequest(parameters, folderId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<UUID>> newGetFolderDocumentsRequest(Uri uri) {
            return new FoldersEndpoint.GetFolderDocumentIdsRequest(uri, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newPostDocumentToFolderRequest(UUID folderId, UUID documentId) {
            return new FoldersEndpoint.PostDocumentToFolderRequest(folderId, documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<Void> newDeleteDocumentFromFolderRequest(UUID folderId, UUID documentId) {
            return new FoldersEndpoint.DeleteDocumentFromFolder(folderId, documentId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Group>> newGetGroupsRequest(GroupsEndpoint.GroupRequestParameters parameters) {
            return new GroupsEndpoint.GetGroupsRequest(parameters, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<Group>> newGetGroupsRequest(Uri uri) {
            return new GroupsEndpoint.GetGroupsRequest(uri, authTokenManager, appCredentials);
        }

        @Override
        public Request<Group> newGetGroupRequest(UUID groupId) {
            return new GroupsEndpoint.GetGroupRequest(groupId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<UserRole>> newGetGroupMembersRequest(GroupsEndpoint.GroupRequestParameters parameters, UUID groupId) {
            return new GroupsEndpoint.GetGroupMembersRequest(parameters, groupId, authTokenManager, appCredentials);
        }

        @Override
        public Request<List<UserRole>> newGetGroupMembersRequest(Uri url){
            return new GroupsEndpoint.GetGroupMembersRequest(url, authTokenManager, appCredentials);

        }

        @Override
        public Request<List<ReadPosition>> newGetRecentlyReadRequest(UUID groupId, UUID fileId, int limit) {
            return new RecentlyReadEndpoint.GetRecentlyReadRequest(groupId, fileId, limit, authTokenManager, appCredentials);
        }

        @Override
        public Request<ReadPosition> newPostRecentlyReadRequest(ReadPosition readPosition) {
            return new RecentlyReadEndpoint.PostRecentlyReadRequest(readPosition, authTokenManager, appCredentials);
        }
    }


    /**
     * Implementation of {@link AuthTokenManager} that uses Shared Preferences to persist the
     * OAuth tokens.
     *
     * <p/>
     *
     * Typical Android applications using the Mendeley SDK won't directly need to deal with this,
     * but this class is left public in case you don't want to use the {@link Mendeley} singleton
     * in your app and you prefer to instantiate the {@link AuthTokenManager} by yourself.
     */
    public static class SharedPreferencesAuthTokenManager implements AuthTokenManager {

        private static final String SHARED_PREFERENCES_NAME = "auth";

        // Shared preferences keys:
        private static final String ACCESS_TOKEN_KEY = "accessToken";
        private static final String REFRESH_TOKEN_KEY = "refreshToken";
        private static final String EXPIRES_AT_KEY = "expiresAtDate";
        private static final String TOKEN_TYPE_KEY = "tokenType";

        private final SharedPreferences preferences;

        public SharedPreferencesAuthTokenManager(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public final void saveTokens(String accessToken, String refreshToken, String tokenType, int expiresIn)  {
            Date expiresAt = generateExpiresAtFromExpiresIn(expiresIn);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(ACCESS_TOKEN_KEY, accessToken);
            editor.putString(REFRESH_TOKEN_KEY, refreshToken);
            editor.putString(TOKEN_TYPE_KEY, tokenType);
            editor.putLong(EXPIRES_AT_KEY, expiresAt.getTime());
            editor.commit();
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        public final void clearTokens() {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(ACCESS_TOKEN_KEY);
            editor.remove(REFRESH_TOKEN_KEY);
            editor.remove(EXPIRES_AT_KEY);
            editor.remove(TOKEN_TYPE_KEY);
            editor.commit();
        }

        @Override
        public final Date getAuthTokenExpirationDate() {
            return new Date(preferences.getLong(EXPIRES_AT_KEY, 0));
        }

        @Override
        public final String getRefreshToken() {
            return preferences.getString(REFRESH_TOKEN_KEY, null);
        }

        @Override
        public final  String getAccessToken() {
            return preferences.getString(ACCESS_TOKEN_KEY, null);
        }

        @Override
        public final String getTokenType() {
            return preferences.getString(TOKEN_TYPE_KEY, null);
        }

        private Date generateExpiresAtFromExpiresIn(int expiresIn) {
            final Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, expiresIn);
            return c.getTime();
        }
    }

}
