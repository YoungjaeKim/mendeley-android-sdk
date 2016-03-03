package com.mendeley.sdk.request;


import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.request.endpoint.DocumentEndpoint;
import com.mendeley.sdk.testUtils.AssertUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TrashRequestTest extends SignedInTest {

    public void test_trashDocument_trashDocumentInServer() throws Exception {
        // GIVEN some documents in the server
        final List<Document> serverDocsBefore = setUpDocumentsInServer(4);

        // WHEN trashing one of them
        final UUID trashingDocId = serverDocsBefore.get(0).id;
        getRequestFactory().newTrashDocumentRequest(trashingDocId).run();

        // THEN the server does not list the document as non trashed documents
        final List<Document> serverDocsAfter= getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;
        for (Document doc : serverDocsAfter) {
            assertFalse(trashingDocId.equals(doc.id));
        }
    }

    public void test_getTrashedDocuments_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents trashed after one date
        final List<Document> existingDocs = setUpDocumentsInServer(6);

        final List<Document> expectedTrashedDocs = new ArrayList<Document>();
        for (int i = 0; i < existingDocs.size() / 2; i++) {
            final Document doc = existingDocs.get(i);
            getRequestFactory().newTrashDocumentRequest(doc.id).run();
            expectedTrashedDocs.add(doc);
        }

        // WHEN requesting trashed docs
        final List<Document> actualTrashedDocs = getRequestFactory().newGetTrashedDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;


        // THEN we receive the trashed docs
        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d1.title.compareTo(d2.title);
            }
        };

        // THEN we have the expected documents
        AssertUtils.assertSameElementsInCollection(expectedTrashedDocs, actualTrashedDocs, comparator);
    }


    public void test_restoreTrashedDocument_restoresDocumentInServer() throws Exception {
        // GIVEN one trashed document in the server
        final Document restoredDoc = setUpDocumentsInServer(1).get(0);
        getRequestFactory().newTrashDocumentRequest(restoredDoc.id);

        // WHEN restoring it
        getRequestFactory().newRestoreTrashedDocumentRequest(restoredDoc.id);

        // THEN the document is no longer trashed
        final List<Document> actual   = getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;
        final List<Document> expected = Arrays.asList(restoredDoc);

        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document d1, Document d2) {
                return d1.title.compareTo(d2.title);
            }
        };
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_deleteTrashedDocument_deletesDocumentFromServer() throws Exception {
        // GIVEN one trashed document in the server
        final Date deletedSince = getServerDate();
        final Document deletingDoc = setUpDocumentsInServer(1).get(0);
        getRequestFactory().newTrashDocumentRequest(deletingDoc.id).run();

        // WHEN deleting it
        getRequestFactory().newDeleteTrashedDocumentRequest(deletingDoc.id).run();

        // THEN the document is permanently deleted
        final DocumentEndpoint.DocumentRequestParameters params = new DocumentEndpoint.DocumentRequestParameters();
        params.deletedSince = deletedSince;

        final List<Document> expectedDeletedDocIds = Collections.singletonList(deletingDoc);
        final List<Document> actualDeletedDocIds = getRequestFactory().newGetDocumentsRequest(params).run().resource;

        Comparator<Document> comparator = new Comparator<Document>() {
            @Override
            public int compare(Document lhs, Document rhs) {
                return lhs.id.compareTo(rhs.id);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDeletedDocIds, actualDeletedDocIds, comparator);
    }

    private List<Document> setUpDocumentsInServer(int docCount) throws Exception {
        // GIVEN some documents in the server
        final List<Document> docs = new LinkedList<Document>();
        for (int i = 0; i < docCount; i++) {
            final String title = "title" + getRandom().nextInt();
            final Document doc = new Document.Builder().
                    setType("book").
                    setTitle(title).
                    setYear(getRandom().nextInt(2000)).
                    setAbstractString("abstract" + getRandom().nextInt()).
                    setSource("source" + getRandom().nextInt()).
                    build();

            getTestAccountSetupUtils().setupDocument(doc);
            docs.add(doc);
        }

        return getRequestFactory().newGetDocumentsRequest((DocumentEndpoint.DocumentRequestParameters) null).run().resource;
    }
}
