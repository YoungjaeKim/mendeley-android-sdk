package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;
import com.mendeley.sdk.util.DateUtils;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TrashEndpointTest extends SignedInTest {

    @SmallTest
    public void test_getTrashDocuments_useTheRightUrl_noParams() {
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").build();
        Uri actual = getRequestFactory().newGetTrashedDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getTrashDocuments_useTheRightUrl_withParams() throws ParseException {
        DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        Date modifiedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
        Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");
        int limit = 7;
        DocumentEndpoint.DocumentRequestParameters.Order order = DocumentEndpoint.DocumentRequestParameters.Order.DESC;
        DocumentEndpoint.DocumentRequestParameters.Sort sort = DocumentEndpoint.DocumentRequestParameters.Sort.MODIFIED;

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("trash")
                .appendQueryParameter("view", view.getValue())
                .appendQueryParameter("group_id", groupId.toString())
                .appendQueryParameter("modified_since", DateUtils.formatMendeleyApiTimestamp(modifiedSince))
                .appendQueryParameter("limit", String.valueOf(limit))
                .appendQueryParameter("order", order.getValue())
                .appendQueryParameter("sort", sort.getValue())
                .appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince))
                .build();

        DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.view = view;
        params.groupId = groupId;
        params.modifiedSince = modifiedSince;
        params.limit = 7;
        params.order = order;
        params.sort = sort;
        params.deletedSince = deletedSince;


        final Uri actualUrl = getRequestFactory().newGetTrashedDocumentsRequest(params).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actualUrl);
    }

    @LargeTest
    public void test_getTrashedDocuments_getTheRightDocuments() throws Exception {
        // GIVEN some deleted documents
        final List<Document> expected = new LinkedList<Document>();
        for (int i = 0; i < 5; i++) {
            final String title = String.format("title %04d", i);
            final Document doc = getTestAccountSetupUtils().setupDocument(createDocument(title));
            getTestAccountSetupUtils().trashDocument(doc.id);
            expected.add(doc);
        }

        // WHEN getting trashed documents
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.sort = DocumentEndpoint.DocumentRequestParameters.Sort.TITLE;

        final List<Document> actual = getRequestFactory().newGetTrashedDocumentsRequest(params).run().resource;

        // THEN we have the expected trashed documents
        AssertUtils.assertDocuments(expected, actual);
    }


    @SmallTest
    public void test_deleteTrashDocument_useTheRightUrl() {
        final UUID docId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").appendPath(docId.toString()).build();
        final Uri actual = getRequestFactory().newDeleteTrashedDocumentRequest(docId).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }

    @LargeTest
    public void test_deleteTrashedDocument_deletesDocInServer() throws Exception {
        // GIVEN some deleted documents
        final List<Document> expected = new LinkedList<Document>();
        for (int i = 0; i < 5; i++) {
            final String title = String.format("title %04d", i);
            final Document doc = getTestAccountSetupUtils().setupDocument(createDocument(title));
            getTestAccountSetupUtils().trashDocument(doc.id);
            expected.add(doc);
        }

        // WHEN deleting one of them
        final Document deletedDocument = expected.remove(getRandom().nextInt(expected.size() -1));
        getRequestFactory().newDeleteTrashedDocumentRequest(deletedDocument.id).run();

        // THEN we have the expected trashed documents
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.sort = DocumentEndpoint.DocumentRequestParameters.Sort.TITLE;
        final List<Document> actual = getRequestFactory().newGetTrashedDocumentsRequest(params).run().resource;

        AssertUtils.assertDocuments(expected, actual);
    }

    @SmallTest
    public void test_restoreTrashedDocument_useTheRightUrl() {
        final UUID docId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("trash").appendPath(docId.toString()).appendPath("restore").build();
        final Uri actual = getRequestFactory().newRestoreTrashedDocumentRequest(docId).getUrl();

        assertEquals("Request url is wrong", expectedUrl, actual);
    }

    @LargeTest
    public void test_restoreTrashedDocuments_deletesDocInServer() throws Exception {
        // GIVEN some deleted documents
        final List<Document> expectedtrashed = new LinkedList<Document>();
        for (int i = 0; i < 5; i++) {
            final String title = String.format("title %04d", i);
            final Document doc = getTestAccountSetupUtils().setupDocument(createDocument(title));
            getTestAccountSetupUtils().trashDocument(doc.id);
            expectedtrashed.add(doc);
        }

        // WHEN restoring one of them
        final Document restoredDocument = expectedtrashed.remove(getRandom().nextInt(expectedtrashed.size() -1));
        getRequestFactory().newRestoreTrashedDocumentRequest(restoredDocument.id).run();

        // THEN we have the expected trashed documents
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.sort = DocumentEndpoint.DocumentRequestParameters.Sort.TITLE;
        final List<Document> actualTrashed = getRequestFactory().newGetTrashedDocumentsRequest(params).run().resource;

        AssertUtils.assertDocuments(expectedtrashed, actualTrashed);

        // AND the expected non-trashed documents
        final List<Document> expectedNonTrashed = Collections.singletonList(restoredDocument);
        final List<Document> actualNonTrashed = getRequestFactory().newGetDocumentsRequest(params).run().resource;

        AssertUtils.assertDocuments(expectedNonTrashed, actualNonTrashed);
    }

    private Document createDocument(String title) throws Exception {
        final Document doc = new Document.Builder().
                setType("book").
                setTitle(title).
                setYear(getRandom().nextInt(2000)).
                setAbstractString("abstract" + getRandom().nextInt()).
                setSource("source" + getRandom().nextInt()).
                build();

        return doc;
    }
}
