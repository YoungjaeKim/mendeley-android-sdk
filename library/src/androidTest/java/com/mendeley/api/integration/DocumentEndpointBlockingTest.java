package com.mendeley.api.integration;

import com.mendeley.api.callbacks.document.DocumentIdList;
import com.mendeley.api.callbacks.document.DocumentList;
import com.mendeley.api.model.Document;
import com.mendeley.api.model.DocumentId;
import com.mendeley.api.params.DocumentRequestParameters;
import com.mendeley.api.params.Page;
import com.mendeley.api.params.Sort;
import com.mendeley.api.params.View;
import com.mendeley.api.testUtils.AssertUtils;
import com.mendeley.api.util.DateUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DocumentEndpointBlockingTest extends EndpointBlockingTest {

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
        final DocumentList response = getSdk().getDocuments();
        final List<Document> actual = response.documents;

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
        final DocumentRequestParameters params = new DocumentRequestParameters();
        params.sort = Sort.TITLE;

        final DocumentList response = getSdk().getDocuments(params);
        final List<Document> actual = response.documents;

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
        final DocumentRequestParameters params = new DocumentRequestParameters();
        params.limit = pageSize;
        params.sort = Sort.TITLE;

        DocumentList response = getSdk().getDocuments(params);

        // THEN we receive a document list...
        for (int page = 0; page < pageCount; page++) {
            //... with the correct docs in each page
            final int begin =  page      * pageSize;
            final int end   = (page + 1) * pageSize;
            final List<Document> expectedInPage = expected.subList(begin, end);
            final List<Document> actualInPage = response.documents;

            AssertUtils.assertDocuments(expectedInPage, actualInPage);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", Page.isValidPage(response.next));
                response = getSdk().getDocuments(response.next);
            }
        }
    }


    // TODO: write tests testing #getDocuments() method with different parameters and sort orders


    public void test_postDocument_createsDocumentInServer() throws Exception {
        // GIVEN a document
        final Document postingDoc = createDocument("posting document");

        // WHEN posting it
        final Document returnedDoc = getSdk().postDocument(postingDoc);

        // THEN we receive the same document back, with id filled
        AssertUtils.assertDocument(postingDoc, returnedDoc);
        assertNotNull(returnedDoc.id);

        // ...and the document exists in the server
        AssertUtils.assertDocuments(getSdk().getDocuments().documents, Arrays.asList(postingDoc));
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
        final Document returnedDoc = getSdk().postDocument(postingDoc);

        // THEN we receive the same document back, with id filled
        AssertUtils.assertDocument(postingDoc, returnedDoc);
        assertNotNull(returnedDoc.id);

        // ...and the document exists in the server
        AssertUtils.assertDocuments(getSdk().getDocuments().documents, Arrays.asList(postingDoc));
    }


    public void test_deleteDocument_removesTheDocumentFromServer() throws Exception {
        // GIVEN some documents in the server
        final List<Document> serverDocsBefore = setUpDocumentsInServer(4);

        // WHEN deleting one of them
        final String deletingDocId = serverDocsBefore.get(0).id;
        getSdk().deleteDocument(deletingDocId);

        // THEN the server does not have the deleted document any more
        final List<Document> serverDocsAfter= getSdk().getDocuments().documents;
        for (Document doc : serverDocsAfter) {
            assertFalse(deletingDocId.equals(doc.id));
        }
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


        final Document returnedDoc = getSdk().patchDocument(docPatching.id, new Date(), docPatching);

        // THEN we receive the patched document
        AssertUtils.assertDocument(docPatching, returnedDoc);

        // ...and the server has updated the doc
        final Document docAfter = getSdk().getDocument(docPatching.id, View.ALL);
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


        final Document returnedDoc = getSdk().patchDocument(docPatching.id, new Date(), docPatching);

        // THEN we receive the patched document
        AssertUtils.assertDocument(docPatching, returnedDoc);

        // ...and the server has updated the doc
        final Document docAfter = getSdk().getDocument(docPatching.id, View.ALL);
        AssertUtils.assertDocument(docPatching, docAfter);
    }


    public void test_getDeletedDocuments_receivesCorrectDocuments() throws Exception {
        // GIVEN some documents deleted after one date
        final String deletedSince = DateUtils.formatMendeleyApiTimestamp(getServerDate());
        final List<Document> existingDocs = setUpDocumentsInServer(6);

        final Set<DocumentId> expectedDeletedDocIds = new HashSet<DocumentId>();
        for (int i = 0; i < existingDocs.size() / 2; i++) {
            final Document doc = existingDocs.get(i);
            getSdk().deleteDocument(doc.id);
            expectedDeletedDocIds.add(new DocumentId.Builder().setDocumentId(doc.id).build());
        }

        // WHEN requesting deleted doc since that date
        DocumentRequestParameters params = new DocumentRequestParameters();
        DocumentIdList deletedDocsIdList = getSdk().getDeletedDocuments(deletedSince, params);


        // THEN we receive the deleted docs
        final Set<DocumentId> actualDeletedDocIds = new HashSet<DocumentId>(deletedDocsIdList.documentIds);
        Comparator<DocumentId> comparator = new Comparator<DocumentId>() {
            @Override
            public int compare(DocumentId lhs, DocumentId rhs) {
                return lhs.id.compareTo(rhs.id);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDeletedDocIds, actualDeletedDocIds, comparator);
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

        return getSdk().getDocuments().documents;
    }
}