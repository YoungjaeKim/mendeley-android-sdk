package com.mendeley.api.request.provider;

import android.util.JsonReader;

import com.mendeley.api.AuthTokenManager;
import com.mendeley.api.ClientCredentials;
import com.mendeley.api.model.Document;
import com.mendeley.api.request.JsonParser;
import com.mendeley.api.request.params.DocumentRequestParameters;
import com.mendeley.api.request.params.View;
import com.mendeley.api.request.GetNetworkRequest;
import com.mendeley.api.request.procedure.PatchNetworkRequest;
import com.mendeley.api.request.procedure.PostNetworkRequest;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.mendeley.api.request.NetworkUtils.API_URL;

/**
 * NetworkProvider class for Documents API calls
 */
public class DocumentNetworkProvider {
	public static String DOCUMENTS_BASE_URL = API_URL + "documents";
	public static String DOCUMENT_TYPES_BASE_URL = API_URL + "document_types";
    public static String IDENTIFIER_TYPES_BASE_URL = API_URL + "identifier_types";
	
	public static SimpleDateFormat patchDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT' Z");

    public DocumentNetworkProvider() {
    }


    /* URLS */

    /**
     * Building the url for deleting document
     *
     * @param documentId the id of the document to delete
     * @return the url string
     */
    public static String getDeleteDocumentUrl(String documentId) {
        return DOCUMENTS_BASE_URL + "/" + documentId;
    }

    /**
     * Building the url for post trash document
     *
     * @param documentId the id of the document to trash
     * @return the url string
     */
    public static String getTrashDocumentUrl(String documentId) {
        return DOCUMENTS_BASE_URL + "/" + documentId + "/trash";
    }

    /**
     * Builds the url for get document
     *
     * @param documentId the document id
     * @return the url string
     */
    public static String getGetDocumentUrl(String documentId, View view) {
        StringBuilder url = new StringBuilder();
        url.append(DOCUMENTS_BASE_URL);
        url.append("/").append(documentId);

        if (view != null) {
            url.append("?").append("view=" + view);
        }

        return url.toString();
    }

    /**
	 * Building the url for get documents
	 * 
	 * @return the url string
	 */
    public static String getGetDocumentsUrl(DocumentRequestParameters params, String deletedSince) {
    	return getGetDocumentsUrl(DOCUMENTS_BASE_URL, params, deletedSince);
    }
    
    /**
	 * Building the url for get trashed documents
	 * 
	 * @return the url string
	 */
    public static String getTrashDocumentsUrl(DocumentRequestParameters params, String deletedSince) {
    	return getGetDocumentsUrl(TrashNetworkProvider.BASE_URL, params, deletedSince);
    }
    
	private static String getGetDocumentsUrl(String baseUrl, DocumentRequestParameters params, String deletedSince)  {
        try {
            StringBuilder url = new StringBuilder();
            url.append(baseUrl);
            StringBuilder paramsString = new StringBuilder();

            if (params != null) {
                boolean firstParam = true;
                if (params.view != null) {
                    paramsString.append("?").append("view=").append(params.view);
                    firstParam = false;
                }
                if (params.groupId != null) {
                    paramsString.append(firstParam ? "?" : "&").append("group_id=").append(params.groupId);
                    firstParam = false;
                }
                if (params.modifiedSince != null) {
                    paramsString.append(firstParam ? "?" : "&").append("modified_since=").append(URLEncoder.encode(params.modifiedSince, "ISO-8859-1"));
                    firstParam = false;
                }
                if (params.limit != null) {
                    paramsString.append(firstParam ? "?" : "&").append("limit=").append(params.limit);
                    firstParam = false;
                }
                if (params.reverse != null) {
                    paramsString.append(firstParam ? "?" : "&").append("reverse=").append(params.reverse);
                    firstParam = false;
                }
                if (params.order != null) {
                    paramsString.append(firstParam ? "?" : "&").append("order=").append(params.order);
                    firstParam = false;
                }
                if (params.sort != null) {
                    paramsString.append(firstParam ? "?" : "&").append("sort=").append(params.sort);
                }
                if (deletedSince != null) {
                    paramsString.append(firstParam ? "?" : "&").append("deleted_since=").append(URLEncoder.encode(deletedSince, "ISO-8859-1"));
                }
            }

            url.append(paramsString.toString());
            return url.toString();

        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
	}

	/**
	 * Building the url for patch document
	 * 
	 * @param documentId the id of the document to patch
	 * @return the url string
	 */
	public static String getPatchDocumentUrl(String documentId) {
		return DOCUMENTS_BASE_URL + "/" + documentId;
	}

    /**
     * @param date the date to format
     * @return date string in the specified format
     */
    private static String formatDate(Date date) {
        if (date == null) {
            return null;
        } else {
            return patchDateFormat.format(date);
        }
    }


    /* PROCEDURES */

    public static class GetDocumentsRequest extends GetNetworkRequest<List<Document>> {
        public GetDocumentsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<Document> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentList(reader);
        }
   }

    public static class GetDeletedDocumentsRequest extends GetNetworkRequest<List<String>> {
        public GetDeletedDocumentsRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected List<String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentIds(reader);
        }
    }


    public static class GetDocumentRequest extends GetNetworkRequest<Document> {
        public GetDocumentRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
        }

        @Override
        protected Document manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocument(reader);
        }
    }

    public static class GetDocumentTypesRequest extends GetNetworkRequest<Map<String, String>> {
        public GetDocumentTypesRequest(String url, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(url, "application/vnd.mendeley-document-type.1+json", authTokenManager, clientCredentials);
        }

        protected Map<String, String> manageResponse(InputStream is) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(is)));
            return JsonParser.parseDocumentTypes(reader);
        }
    }

    public static class PostDocumentRequest extends PostNetworkRequest<Document> {

        final private Document doc;

        public PostDocumentRequest(Document doc, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(DOCUMENTS_BASE_URL, "application/vnd.mendeley-document.1+json", authTokenManager, clientCredentials);
            this.doc = doc;
        }

        @Override
        protected String obtainJsonToPost() throws JSONException {
            return JsonParser.jsonFromDocument(doc);
        }

        @Override
        protected Document parseJsonString(String jsonString) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
            return JsonParser.parseDocument(reader);
        }
    }

    public static class PatchDocumentRequest extends PatchNetworkRequest<Document> {
        private final Document document;

        public PatchDocumentRequest(String documentId, Document document, Date date, AuthTokenManager authTokenManager, ClientCredentials clientCredentials) {
            super(getPatchDocumentUrl(documentId), "application/vnd.mendeley-document.1+json", formatDate(date), authTokenManager, clientCredentials);
            this.document = document;
        }

        @Override
        protected String obtainJsonToPost() throws JSONException {
            return JsonParser.jsonFromDocument(document);
        }

        @Override
        protected Document processJsonString(String jsonString) throws JSONException, IOException {
            final JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())));
            return JsonParser.parseDocument(reader);
        }
    }
}
