package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.ClientCredentials;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PostAuthorizedRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /profiles endpoint.
 * {@see http://dev.mendeley.com/methods/#profiles}
 */
public class ProfilesEndpoint {
	public static final String PROFILES_URL = MENDELEY_API_BASE_URL + "profiles/";
    public static final String PROFILE_CONTENT_TYPE = "application/vnd.mendeley-profiles.1+json";
    public static final String NEW_PROFILE_CONTENT_TYPE = "application/vnd.mendeley-new-profile.1+json";

    public ProfilesEndpoint() {
    }

    public static class GetProfileRequest extends GetAuthorizedRequest<Profile> {
        public GetProfileRequest(String profileId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(ProfilesEndpoint.PROFILES_URL + profileId), authTokenManager, clientCredentials);
        }

        @Override
        protected Profile manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.profileFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", PROFILE_CONTENT_TYPE);
        }
    }

    public static class PostProfileRequest extends PostAuthorizedRequest<Profile> {
        private final Profile profile;
        private final String password;

        public PostProfileRequest(AuthTokenManager authTokenManager, ClientCredentials clientCredentials, Profile profile, String password) {
            super(Uri.parse(PROFILES_URL), authTokenManager, clientCredentials);
            this.profile = profile;
            this.password = password;
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", PROFILE_CONTENT_TYPE);
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(NEW_PROFILE_CONTENT_TYPE), JsonParser.profileToJson(profile, password).toString());
        }

        @Override
        protected Profile manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.profileFromJson(reader);
        }
    }

    public static class DeleteProfileRequest extends DeleteAuthorizedRequest<Void> {

        public DeleteProfileRequest(String profileId, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(Uri.parse(PROFILES_URL + "/" + profileId), authTokenManager, clientCredentials);
        }
    }

}
