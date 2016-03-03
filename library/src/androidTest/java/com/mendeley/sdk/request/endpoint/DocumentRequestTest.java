package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;
import com.mendeley.sdk.util.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DocumentRequestTest extends SignedInTest {

    @SmallTest
    public void test_getDocument_usesTheRightUrl_withoutView() throws Exception {

        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendPath(documentId.toString()).build();

        Uri actual = getRequestFactory().newGetDocumentRequest(documentId, null).getUrl();

        assertEquals("Get document url without parameters is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getDocument_usesTheRightUrl_with_View() throws Exception {

        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendPath(documentId.toString()).appendQueryParameter("view", "client").build();

        Uri actual = getRequestFactory().newGetDocumentRequest(documentId, DocumentEndpoint.DocumentRequestParameters.View.CLIENT).getUrl();

        assertEquals("Get document url with parameters is wrong", expectedUrl, actual);
    }


    public void test_getDocuments_withoutParameters_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents
        final List<Document> expected = new LinkedList<Document>();

        for (int i = 0; i < 5; i++) {
            final String title = "title" + getRandom().nextInt();
            final Document doc = createDocument(title);
            getTestAccountSetupUtils().setupDocument(doc);
            expected.add(doc);
        }

        // WHEN getting documents
        final List<Document> actual = getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;

        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d1.title.compareTo(d2.title);
            }
        };

        // THEN we have the expected documents
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getDocuments_sortedByTitle_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents
        final List<Document> expected = new LinkedList<Document>();
        for (int i = 0; i < 5; i++) {
            final String title = String.format("title %04d", i);
            final Document doc = createDocument(title);
            getTestAccountSetupUtils().setupDocument(doc);
            expected.add(doc);
        }

        // WHEN getting documents
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.sort = DocumentEndpoint.DocumentRequestParameters.Sort.TITLE;

        final List<Document> actual = getRequestFactory().newGetDocumentsRequest(params).run().resource;

        // THEN we have the expected documents
        AssertUtils.assertDocuments(expected, actual);
    }

    public void test_getDocuments_whenMoreThanOnePage_receivesCorrectDocuments() throws Exception {
        // GIVEN a number of documents greater than the page size
        final int pageSize = 4;
        final int pageCount = 3;
        final int docsCount = pageSize * pageCount;

        final List<Document> expected = new LinkedList<Document>();
        for (int i = 0; i < docsCount; i++) {
            final String title = String.format("title %04d", i);
            final Document doc = createDocument(title);
            getTestAccountSetupUtils().setupDocument(doc);
            expected.add(doc);
        }

        // WHEN getting documents
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.limit = pageSize;
        params.sort = DocumentEndpoint.DocumentRequestParameters.Sort.TITLE;

        Request<List<Document>>.Response response = getRequestFactory().newGetDocumentsRequest(params).run();

        // THEN we receive a document list...
        for (int page = 0; page < pageCount; page++) {
            //... with the correct docs in each page
            final int begin =  page      * pageSize;
            final int end   = (page + 1) * pageSize;
            final List<Document> expectedInPage = expected.subList(begin, end);
            final List<Document> actualInPage = response.resource;

            AssertUtils.assertDocuments(expectedInPage, actualInPage);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().newGetDocumentsRequest(response.next).run();
            }
        }
    }


    // TODO: write tests testing #getDocuments() method with different parameters and sort orders


    public void test_postDocument_createsDocumentInServer() throws Exception {
        // GIVEN a document
        final Document postingDoc = createDocument("posting document");

        // WHEN posting it
        final Document returnedDoc = getRequestFactory().newPostDocumentRequest(postingDoc).run().resource;

        // THEN we receive the same document back, with id filled
        AssertUtils.assertDocument(postingDoc, returnedDoc);
        assertNotNull(returnedDoc.id);

        // ...and the document exists in the server
        AssertUtils.assertDocuments(getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource, Arrays.asList(postingDoc));
    }

    public void test_postDocument_withStrangeCharacters_createsDocumentInServer() throws Exception {
        // GIVEN a document
        final Document postingDoc = new Document.Builder().
                setType("book").
                setTitle("österreichische ña áéíóuú cÑulo").
                setSource("österrá éíóuú eichiÑ sche ña  cÑulo").
                setYear(getRandom().nextInt(2000)).
                setAbstractString("österrá éíóuú eichi").
                build();

        // WHEN posting it
        final Document returnedDoc = getRequestFactory().newPostDocumentRequest(postingDoc).run().resource;

        // THEN we receive the same document back, with id filled
        AssertUtils.assertDocument(postingDoc, returnedDoc);
        assertNotNull(returnedDoc.id);

        // ...and the document exists in the server
        AssertUtils.assertDocuments(getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource, Arrays.asList(postingDoc));
    }

    @SmallTest
    public void test_deleteDocument_usesTheRightUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");
        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendPath(documentId.toString()).build();
        Uri actual =  getRequestFactory().newDeleteDocumentRequest(documentId).getUrl();

        assertEquals("Documents url is wrong", expectedUrl, actual);
    }

    public void test_deleteDocument_removesTheDocumentFromServer() throws Exception {
        // GIVEN some documents in the server
        final List<Document> serverDocsBefore = setUpDocumentsInServer(4);

        // WHEN deleting one of them
        final UUID deletingDocId = serverDocsBefore.get(0).id;
        getRequestFactory().newDeleteDocumentRequest(deletingDocId).run();

        // THEN the server does not have the deleted document any more
        final List<Document> serverDocsAfter= getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;
        for (Document doc : serverDocsAfter) {
            assertFalse(deletingDocId.equals(doc.id));
        }
    }

    @SmallTest
    public void test_getPatchDocument_usesTheRightUrl() throws Exception {
        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendPath(documentId.toString()).build();
        final Uri url = getRequestFactory().newPatchDocumentRequest(documentId, null, null).getUrl();

        assertEquals("Patch document url is wrong", expectedUrl, url);
    }

    public void test_patchDocument_updatesTheDocumentFromServer() throws Exception {
        // GIVEN ine document in the server
        final Document docBefore = setUpDocumentsInServer(1).get(0);

        // WHEN patching in
        final Document docPatching = new Document.Builder(docBefore)
                .setTitle(docBefore.title + "updated")
                .setAbstractString(docBefore.abstractString + "updated")
                .setSource(docBefore.source + "updated")
                .setYear(getRandom().nextInt(2000))
                .build();


        final Document returnedDoc = getRequestFactory().newPatchDocumentRequest(docPatching.id, null, docPatching).run().resource;

        // THEN we receive the patched document
        AssertUtils.assertDocument(docPatching, returnedDoc);

        // ...and the server has updated the doc
        final Document docAfter = getRequestFactory().newGetDocumentRequest(docPatching.id, DocumentEndpoint.DocumentRequestParameters.View.ALL).run().resource;
        AssertUtils.assertDocument(docPatching, docAfter);
    }

    public void test_patchDocument_withStrangeCharacters_updatesTheDocumentFromServer() throws Exception {
        // GIVEN ine document in the server
        final Document docBefore = setUpDocumentsInServer(1).get(0);

        // WHEN patching in
        final Document docPatching = new Document.Builder(docBefore)
                .setTitle("österreichische ña áéíóuú cÑulo")
                .setSource("österrá éíóuú eichiÑ sche ña  cÑulo")
                .setAbstractString("österrá éíóuú eichi")
                .setYear(getRandom().nextInt(2000))
                .build();


        final Document returnedDoc = getRequestFactory().newPatchDocumentRequest(docPatching.id, null, docPatching).run().resource;

        // THEN we receive the patched document
        AssertUtils.assertDocument(docPatching, returnedDoc);

        // ...and the server has updated the doc
        final Document docAfter = getRequestFactory().newGetDocumentRequest(docPatching.id, DocumentEndpoint.DocumentRequestParameters.View.ALL).run().resource;
        AssertUtils.assertDocument(docPatching, docAfter);
    }

    @SmallTest
    public void test_getDeletedDocuments_usesTheRightUrl_withLotsOfParams() throws Exception {
        DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        Date modifiedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
        Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");
        int limit = 7;
        DocumentEndpoint.DocumentRequestParameters.Order order = DocumentEndpoint.DocumentRequestParameters.Order.DESC;
        DocumentEndpoint.DocumentRequestParameters.Sort sort = DocumentEndpoint.DocumentRequestParameters.Sort.MODIFIED;

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("documents")
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

        final Uri url = getRequestFactory().newGetDocumentsRequest(params).getUrl();

        assertEquals("Get documents url with parameters is wrong", expectedUrl, url);
    }

    @SmallTest
    public void test_getDeletedDocuments_usesTheRightUrl_withOnlyView() throws Exception {
        final DocumentEndpoint.DocumentRequestParameters.View view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendQueryParameter("view", view.getValue()).build();

        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.view = DocumentEndpoint.DocumentRequestParameters.View.ALL;
        final Uri actual = getRequestFactory().newGetDocumentsRequest(params).getUrl();

        assertEquals("Get documents url with parameters is wrong", expectedUrl, actual);
    }

    public void test_getDeletedDocuments_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents deleted after one date
        final Date deletedSince = getServerDate();
        final List<Document> existingDocs = setUpDocumentsInServer(6);

        final Set<Document> expectedDeletedDocs = new HashSet<>();
        for (int i = 0; i < existingDocs.size() / 2; i++) {
            final Document doc = existingDocs.get(i);
            getRequestFactory().newDeleteDocumentRequest(doc.id).run();
            expectedDeletedDocs.add(doc);
        }

        // WHEN requesting deleted doc since that date
        DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.deletedSince = deletedSince;

        List<Document> deletedDocsList = getRequestFactory().newGetDocumentsRequest(params).run().resource;


        // THEN we receive the deleted docs
        final Set<Document> actualDeletedDocs = new HashSet<>(deletedDocsList);
        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document lhs, Document rhs) {
                return lhs.id.compareTo(rhs.id);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDeletedDocs, actualDeletedDocs, comparator);
    }

    // TODO: create test for trash document

    @SmallTest
    public void test_trashedDocument_usesTheRightUrl() throws Exception {
        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");
        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("documents").appendPath(documentId.toString()).appendPath("trash").build();
        Uri actual =  getRequestFactory().newTrashDocumentRequest(documentId).getUrl();

        assertEquals("Documents url is wrong", expectedUrl, actual);
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

    private List<Document> setUpDocumentsInServer(int docCount) throws Exception {
        // GIVEN some documents in the server
        final List<Document> docs = new LinkedList<Document>();
        for (int i = 0; i < docCount; i++) {
            final String title = "title" + getRandom().nextInt();
            final Document doc = createDocument(title);
            getTestAccountSetupUtils().setupDocument(doc);
            docs.add(doc);
        }

        return getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;
    }
}
