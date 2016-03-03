package com.mendeley.sdk.testUtils;

import android.util.JsonReader;

import com.mendeley.sdk.AuthTokenManager;
import com.mendeley.sdk.BuildConfig;
import com.mendeley.sdk.RequestsFactory;
import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.request.JsonParser;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;
import com.mendeley.sdk.request.endpoint.FoldersEndpoint;
import com.mendeley.sdk.request.endpoint.GroupsEndpoint;
import com.mendeley.sdk.util.DateUtils;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class for setting the test account for running the tests
 */
public class TestAccountSetupUtils{

    private final RequestsFactory requestFactory;
    private final AuthTokenManager authTokenManager;

    public TestAccountSetupUtils(AuthTokenManager authTokenManager, RequestsFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.authTokenManager = authTokenManager;
    }

    /**
     * After calling this method the account should be clean (no docs, no groups, nothing...)
     */
    public void cleanAll() {
        cleanDocs();
        cleanFolders();
    }

    private void cleanDocs() {
        try {
            // delete non-trashed docs
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Document doc: requestFactory.newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                requestFactory.newDeleteDocumentRequest(doc.id).run();
            }

            // delete trashed docs
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Document doc: requestFactory.newGetTrashedDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                requestFactory.newDeleteTrashedDocumentRequest(doc.id).run();
            }

            // ensure no documents at all (trashed or deleted)
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            Assert.assertEquals("Expected empty list of non trashed docs in server", 0, requestFactory.newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource.size());
            Assert.assertEquals("Expected empty list of trashed docs in server", 0, requestFactory.newGetTrashedDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource.size());

        } catch (Exception e) {
            throw new TestAccountSetupException(e);
        }
    }

    private void cleanFolders() {
        try {
            // delete parent folders as sub folders will be deleted as well
            // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
            for (Folder folder: requestFactory.newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters) null).run().resource) {
                // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory...
                if (folder.parentId == null) {
                    requestFactory.newDeleteFolderRequest(folder.id).run();
                }
            }

            Assert.assertEquals("Expected empty list of folders in server", 0, requestFactory.newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters)null).run().resource.size());

        } catch (Exception e) {
            throw new TestAccountSetupException(e);
        }
    }

    public Document setupDocument(Document doc) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.newPostDocumentRequest(doc).run().resource;
    }

    public void trashDocument(UUID docId) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
       requestFactory.newTrashDocumentRequest(docId).run();
    }

    public Annotation setupAnnotation(Annotation annotation) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.newPostAnnotationRequest(annotation).run().resource;
    }

    public File setupFile(UUID docId, String fileName, InputStream inputStream) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.newPostFileWithBinaryRequest("application/pdf", docId, inputStream, fileName).run().resource;
    }

    public Folder setupFolder(Folder folder) throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.newPostFolderRequest(folder).run().resource;
    }

    public List<Group> getGroups() throws MendeleyException {
        // FIXME: do not delegate into the requestFactory to this, because we are testing the requestFactory this should receive a JSON and post it using HTTP
        return requestFactory.newGetGroupsRequest(new GroupsEndpoint.GroupRequestParameters()).run().resource;
    }


    public ReadPosition setupReadingPosition(UUID fileId, int page, int verticalPosition, Date date) throws Exception{
        HttpsURLConnection con = null;
        OutputStream os = null;
        BufferedWriter writer = null;

        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("file_id", fileId);
            jsonObject.put("page", page);
            jsonObject.put("vertical_position", verticalPosition);
            jsonObject.put("date", DateUtils.formatMendeleyApiTimestamp(date));


            final URL callUrl = new URL(BuildConfig.WEB_API_BASE_URL + "/" + "recently_read");

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");

            con.connect();

            os = con.getOutputStream();

            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code posting recently read position: " + responseCode + " " + con.getResponseMessage());
            }

            final JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
            return JsonParser.readPositionFromJson(reader);
        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (os != null) {
                    os.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {}
        }
    }

    public List<ReadPosition> getAllReadingPositions() throws Exception {
        HttpsURLConnection con = null;
        InputStream is = null;

        try {
            final URL callUrl = new URL(BuildConfig.WEB_API_BASE_URL + "/" + "recently_read");

            con = (HttpsURLConnection) callUrl.openConnection();
            con.setRequestMethod("GET");
            con.addRequestProperty("Authorization", "Bearer " + authTokenManager.getAccessToken());
            con.addRequestProperty("Content-type", "application/vnd.mendeley-recently-read.1+json");

            con.connect();

            is = con.getInputStream();

            final int responseCode = con.getResponseCode();
            if (responseCode < 200 && responseCode >= 300) {
                throw new Exception("Invalid response code getting recently read position: " + responseCode + " " + con.getResponseMessage());
            }

            final JsonReader reader = new JsonReader(new InputStreamReader(con.getInputStream()));
            return JsonParser.readPositionsFromJson(reader);
        } finally {
            try {
                if (con != null) {
                    con.disconnect();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) {}
        }
    }


    /**
     * Exceptions that may happen when trying to set up the testing scenario
     */
    public static class TestAccountSetupException extends RuntimeException {
        public TestAccountSetupException(Exception cause) {
            super("A problem happened when trying to setup the testing scenario", cause);
        }
    }
}
