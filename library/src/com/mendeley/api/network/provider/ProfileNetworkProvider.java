package com.mendeley.api.network.provider;

import org.json.JSONException;

import com.mendeley.api.auth.AccessTokenProvider;
import com.mendeley.api.callbacks.profile.GetProfileCallback;
import com.mendeley.api.exceptions.MendeleyException;
import com.mendeley.api.model.Profile;
import com.mendeley.api.network.Environment;
import com.mendeley.api.network.JsonParser;
import com.mendeley.api.network.task.GetNetworkProcedure;
import com.mendeley.api.network.task.GetNetworkTask;

import static com.mendeley.api.network.NetworkUtils.*;

/**
 * NetworkProvider class for Profile API calls
 */
public class ProfileNetworkProvider {
	private static String profilesUrl = API_URL + "profiles/";

    private final Environment environment;
    private final AccessTokenProvider accessTokenProvider;

    public ProfileNetworkProvider(Environment environment, AccessTokenProvider accessTokenProvider) {
        this.environment = environment;
        this.accessTokenProvider = accessTokenProvider;
    }

    /* ASYNC */

    public void doGetMyProfile(GetProfileCallback callback) {
		String[] paramsArray = new String[] { profilesUrl + "me" };
        GetProfileTask task = new GetProfileTask(callback);
		task.executeOnExecutor(environment.getExecutor(), paramsArray);
	}
	
	/**
	 * @param profileId the profile to get
	 */
    public void doGetProfile(String profileId, GetProfileCallback callback) {
		String[] paramsArray = new String[] { profilesUrl + profileId };
        GetProfileTask task = new GetProfileTask(callback);
		task.executeOnExecutor(environment.getExecutor(), paramsArray);
	}

    /* BLOCKING */

    public Profile doGetMyProfile() throws MendeleyException {
        GetProfileProcedure proc = new GetProfileProcedure();
        proc.run(profilesUrl + "me");
        return proc.getResult();
    }

    /**
     * @param profileId the profile to get
     */
    public Profile doGetProfile(String profileId) throws MendeleyException {
        GetProfileProcedure proc = new GetProfileProcedure();
        proc.run(profilesUrl + profileId);
        return proc.getResult();
    }

    /* TASKS */

    private class GetProfileTask extends GetNetworkTask {
        private Profile profile;

        private final GetProfileCallback callback;

        private GetProfileTask(GetProfileCallback callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            profile = JsonParser.parseProfile(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-profiles.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }

        @Override
        protected void onSuccess() {
            callback.onProfileReceived(profile);
        }

        @Override
        protected void onFailure(MendeleyException exception) {
            callback.onProfileNotReceived(exception);
        }
    }

    private class GetProfileProcedure extends GetNetworkProcedure {
        private Profile profile;

        private Profile getResult() {
            return profile;
        }

        @Override
        protected void processJsonString(String jsonString) throws JSONException {
            profile = JsonParser.parseProfile(jsonString);
        }

        @Override
        protected String getContentType() {
            return "application/vnd.mendeley-profiles.1+json";
        }

        @Override
        protected AccessTokenProvider getAccessTokenProvider() {
            return accessTokenProvider;
        }
    }
}
