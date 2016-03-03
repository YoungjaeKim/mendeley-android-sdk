package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.Request;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /profiles endpoint.
 * {@see http://dev.mendeley.com/methods/#profiles}
 */
public class ProfilesEndpoint {
	public static String PROFILES_URL = MENDELEY_API_BASE_URL + "profiles/";

    public ProfilesEndpoint() {
    }

    public static class GetProfileRequest extends GetAuthorizedRequest<Profile> {

        public GetProfileRequest(AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(ProfilesEndpoint.PROFILES_URL + "me"), authTokenManager, appCredentials);
        }

        public GetProfileRequest(UUID profileId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(ProfilesEndpoint.PROFILES_URL + profileId), authTokenManager, appCredentials);
        }

        @Override
        protected Profile manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.profileFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", "application/vnd.mendeley-profiles.1+json");
        }
    }
}
