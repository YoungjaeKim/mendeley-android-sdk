package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.UserRole;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.Request;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /groups endpoint.
 * {@see http://dev.mendeley.com/methods/#groups}
 */
public class GroupsEndpoint {
    private static final String GROUP_BASE_URL = MENDELEY_API_BASE_URL + "groups";
    public static final String GROUP_CONTENT_TYPE = "application/vnd.mendeley-group.1+json";
    public static final String MEMBERSHIP_CONTENT_TYPE = "application/vnd.mendeley-membership.1+json";

    public static class GetGroupsRequest extends GetAuthorizedRequest<List<Group>> {
        private static Uri getGetGroupsUrl(GroupRequestParameters params) {
            final Uri.Builder bld = Uri.parse(GROUP_BASE_URL).buildUpon();
            if (params == null) {
                return bld.build();
            }
            return params.appendToUi(bld.build());
        }

        public GetGroupsRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetGroupsRequest(GroupRequestParameters params, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this(getGetGroupsUrl(params), authTokenManager, appCredentials);
        }

        @Override
        protected List<Group> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.groupsFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", GROUP_CONTENT_TYPE);
        }
    }

    public static class GetGroupRequest extends GetAuthorizedRequest<Group> {
        public GetGroupRequest(UUID groupId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(GROUP_BASE_URL + "/" + groupId), authTokenManager, appCredentials);
        }

        @Override
        protected Group manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.groupFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", GROUP_CONTENT_TYPE);
        }
    }

    public static class GetGroupMembersRequest extends GetAuthorizedRequest<List<UserRole>> {
        private static Uri getGetGroupMembersUrl(GroupRequestParameters params, UUID groupId) {
            final Uri.Builder bld = Uri.parse(GROUP_BASE_URL + "/" + groupId + "/members").buildUpon();
            if (params == null) {
                return bld.build();
            }
            return params.appendToUi(bld.build());
        }

        public GetGroupMembersRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetGroupMembersRequest(GroupRequestParameters parameters, UUID groupId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this(getGetGroupMembersUrl(parameters, groupId), authTokenManager, appCredentials);
        }

        @Override
        protected List<UserRole> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.userRolesFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", MEMBERSHIP_CONTENT_TYPE);
        }
    }

    public static class GroupRequestParameters {
        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;

        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();

            if (limit != null) {
                bld.appendQueryParameter("limit", String.valueOf(limit));
            }

            return bld.build();
        }
    }
}
