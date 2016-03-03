package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.util.JsonReader;

import com.mendeley.sdk.AppCredentials;
import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.request.DeleteAuthorizedRequest;
import com.mendeley.sdk.request.GetAuthorizedRequest;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.PatchAuthorizedRequest;
import com.mendeley.sdk.request.PostAuthorizedRequest;
import com.mendeley.sdk.util.DateUtils;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

/**
 * Class with the implementation of typical {@link Request}s against the /annotation endpoint.
 * {@see http://dev.mendeley.com/methods/#annotations}
 */
public class AnnotationsEndpoint {

    public static String ANNOTATIONS_BASE_URL = MENDELEY_API_BASE_URL + "annotations";
    private static String ANNOTATIONS_CONTENT_TYPE = "application/vnd.mendeley-annotation.1+json";

    public static class GetAnnotationRequest extends GetAuthorizedRequest<Annotation> {
        public GetAnnotationRequest(UUID annotationId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId), authTokenManager, appCredentials);
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.annotationFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }
    }

    public static class GetAnnotationsRequest extends GetAuthorizedRequest<List<Annotation>> {

        private static Uri getAnnotationsUrl(AnnotationRequestParameters params) {
            final Uri uri = Uri.parse(ANNOTATIONS_BASE_URL);
            return params != null ? params.appendToUi(uri) : uri;
        }

        public GetAnnotationsRequest(Uri url, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(url, authTokenManager, appCredentials);
        }

        public GetAnnotationsRequest(AnnotationRequestParameters parameters, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            this(getAnnotationsUrl(parameters), authTokenManager, appCredentials);
        }

        @Override
        protected List<Annotation> manageResponse(InputStream is) throws JSONException, IOException, ParseException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.annotationsFromJson(reader);
        }

        @Override
        protected void appendHeaders(Map<String, String> headers) {
            headers.put("Content-type", ANNOTATIONS_CONTENT_TYPE);
        }
   }

    public static class PostAnnotationRequest extends PostAuthorizedRequest<Annotation> {
        private final Annotation annotation;

        public PostAnnotationRequest(Annotation annotation, AuthTokenManager authTokenManager, AppCredentials appCredentials){
            super(Uri.parse(ANNOTATIONS_BASE_URL), authTokenManager, appCredentials);
            this.annotation = annotation;
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(ANNOTATIONS_CONTENT_TYPE), JsonParser.annotationToJson(annotation).toString());
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.annotationFromJson(reader);
        }

    }

    public static class PatchAnnotationRequest extends PatchAuthorizedRequest<Annotation> {
        private final Annotation annotation;

        public PatchAnnotationRequest(UUID annotationId, Annotation annotation, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(getUrl(annotationId), null, authTokenManager, appCredentials);
            this.annotation = annotation;
        }

        private static Uri getUrl(UUID annotationId) {
            return Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId);
        }

        @Override
        protected RequestBody getBody() throws JSONException {
            return RequestBody.create(MediaType.parse(ANNOTATIONS_CONTENT_TYPE), JsonParser.annotationToJson(annotation).toString());
        }

        @Override
        protected Annotation manageResponse(InputStream is) throws Exception {
            final JsonReader reader = new JsonReader(new InputStreamReader(is));
            return JsonParser.annotationFromJson(reader);
        }

    }

    public static class DeleteAnnotationRequest extends DeleteAuthorizedRequest<Void> {
        public DeleteAnnotationRequest(UUID annotationId, AuthTokenManager authTokenManager, AppCredentials appCredentials) {
            super(Uri.parse(ANNOTATIONS_BASE_URL + "/" + annotationId), authTokenManager, appCredentials);
        }
    }

    /**
     * Parameters for requests to retrieve annotations.
     * <p>
     * Uninitialised properties will be ignored.
     */
    public static class AnnotationRequestParameters {
        public UUID documentId;

        public UUID groupId;

        public Boolean includeTrashed;

        /**
         * Returns only annotations modified since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date modifiedSince;

        /**
         * Returns only annotations deleted since this timestamp. Should be supplied in ISO 8601 format.
         */
        public Date deletedSince;

        /**
         * The maximum number of items on the page. If not supplied, the default is 20. The largest allowable value is 500.
         */
        public Integer limit;


        Uri appendToUi(Uri uri) {
            final Uri.Builder bld = uri.buildUpon();


            if (documentId != null) {
                bld.appendQueryParameter("document_id", documentId.toString());
            }
            if (groupId != null) {
                bld.appendQueryParameter("group_id", groupId.toString());
            }
            if (includeTrashed != null) {
                bld.appendQueryParameter("include_trashed", String.valueOf(includeTrashed));
            }
            if (modifiedSince != null) {
                bld.appendQueryParameter("modified_since", DateUtils.formatMendeleyApiTimestamp(modifiedSince));
            }
            if (deletedSince != null) {
                bld.appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince));
            }
            if (limit != null) {
                bld.appendQueryParameter("limit", String.valueOf(limit));
            }

            return bld.build();
        }

    }
}
