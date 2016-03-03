package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mendeley.sdk.Request.MENDELEY_API_BASE_URL;

public class FolderRequestTest extends SignedInTest {

    @SmallTest
    public void test_GetFoldersRequest_usesCorrectUrl_withParameters() throws Exception {
        final UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendQueryParameter("group_id", groupId.toString()).build();

        FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = groupId;
        final Uri actualUri = getRequestFactory().newGetFoldersRequest(params).getUrl();

        assertEquals("Get folders url with parameters is wrong", expectedUrl, actualUri);
    }


    public void test_getFolders_withoutParameters_receivesCorrectFolders() throws Exception {
        // GIVEN some folders
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);

        for (int i = 0; i < 3; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders
        final List<Folder> actual = getRequestFactory().newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters) null).run().resource;

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFolders_withParameters_receivesCorrectFolders() throws Exception {
        // GIVEN some folders
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);

        for (int i = 0; i < 3; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders with parameters
        FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = null;
        params.limit = 10;
        final List<Folder> actual = getRequestFactory().newGetFoldersRequest(params).run().resource;

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFolders_whenMoreThanOnePage_receivesCorrectFolders() throws Exception {

        // GIVEN a number of folders greater than the page size
        final int pageSize = 4;
        final int pageCount = 2;
        final List<Folder> expected = new LinkedList<Folder>();

        Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
        expected.add(parentFolder);
        expected.add(getTestAccountSetupUtils().setupFolder(createParentFolder()));

        for (int i = 0; i < 5; i++) {
            final Folder subFolder = createSubFolder(parentFolder.id);
            getTestAccountSetupUtils().setupFolder(subFolder);
            expected.add(subFolder);
        }

        // WHEN getting folders
        final FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.limit = pageSize;

        final List<Folder> actual = new LinkedList<Folder>();
        Request<List<Folder>>.Response response = getRequestFactory().newGetFoldersRequest(params).run();

        // THEN we receive a folder list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().newGetFoldersRequest(response.next).run();
            }
        }

        Comparator<Folder> comparator = new Comparator<Folder>() {
            @Override
            public int compare(Folder f1, Folder f2) {
                return f1.name.compareTo(f2.name);
            }
        };

        // THEN we have the expected folders
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_postFolder_createsFolderInServer() throws Exception {

        // GIVEN a folder
        final Folder postingFolder = createParentFolder();

        // WHEN posting it
        final Folder returnedFolder = getRequestFactory().newPostFolderRequest(postingFolder).run().resource;

        // THEN we receive the same folder back, with id filled
        AssertUtils.assertFolder(postingFolder, returnedFolder);
        assertNotNull(returnedFolder.id);

        // ...and the folder exists in the server
        AssertUtils.assertFolders(getRequestFactory().newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters) null).run().resource, Arrays.asList(postingFolder));
    }

    @SmallTest
    public void test_DeleteFolderUrl_usesrightUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId.toString()).build();
        final Uri url = getRequestFactory().newDeleteFolderRequest(folderId).getUrl();

        assertEquals("Delete folder url is wrong", expectedUrl, url);
    }

    @LargeTest
    public void test_deleteFolder_removesTheFolderFromServer() throws Exception {
        // GIVEN some folders
        final List<Folder> serverFoldersBefore = new LinkedList<Folder>();

        for (int i = 0; i < 5; i++) {
            Folder parentFolder = getTestAccountSetupUtils().setupFolder(createParentFolder());
            serverFoldersBefore.add(parentFolder);
        }

        // WHEN deleting one of them
        final UUID deletingFolderId = serverFoldersBefore.get(0).id;
        getRequestFactory().newDeleteFolderRequest(deletingFolderId).run();

        // THEN the server does not have the deleted folder any more
        final List<Folder> serverFoldersAfter = getRequestFactory().newGetFoldersRequest((FoldersEndpoint.FolderRequestParameters) null).run().resource;
        for (Folder folder : serverFoldersAfter) {
            assertFalse(deletingFolderId.equals(folder.id));
        }
    }

    @SmallTest
    public void test_patchFolder_usesTheRightUrl() throws Exception {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId.toString()).build();
        final Uri actualUrl = getRequestFactory().newPatchFolderRequest(folderId, null).getUrl();

        assertEquals("Patch folder url is wrong", expectedUrl, actualUrl);
    }

    public void test_patchFolder_updatesTheFolderOnServer() throws Exception {
        // GIVEN a folder
        Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // WHEN patched
        final Folder folderPatched = new Folder.Builder(folder)
                .setName(folder.name + "updated")
                .build();

        final Folder returnedFolder = getRequestFactory().newPatchFolderRequest(folder.id, folderPatched).run().resource;

        // THEN we receive the patched folder
        AssertUtils.assertFolder(folderPatched, returnedFolder);

        // ...and the server has updated the folder
        final Folder folderAfter = getRequestFactory().newGetFolderRequest(folderPatched.id).run().resource;
        AssertUtils.assertFolder(folderPatched, folderAfter);
    }

    @SmallTest
    public void test_getDocumentsInFolder_usesTheCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");
        final UUID groupdId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        final int limit = 69;

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("folders")
                .appendPath(folderId.toString())
                .appendPath("documents")
                .appendQueryParameter("group_id", groupdId.toString())
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        final FoldersEndpoint.FolderRequestParameters params = new FoldersEndpoint.FolderRequestParameters();
        params.groupId = groupdId;
        params.limit = limit;

        final Uri actualUrl = getRequestFactory().newGetFolderDocumentsRequest(params, folderId).getUrl();

        assertEquals("Get folder document ids url is wrong", expectedUrl, actualUrl);
    }

    public void test_getDocumentsInFolder_receivesTheCorrectDocumentIds() throws Exception {

        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents in that folder
        final List<UUID> expectedDocIds = new LinkedList<>();

        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            getRequestFactory().newPostDocumentToFolderRequest(folder.id, document.id).run();
            expectedDocIds.add(document.id);
        }

        // WHEN getting the documents in the folder
        final List<UUID> actualDocIds = getRequestFactory().newGetFolderDocumentsRequest(null, folder.id).run().resource;

        Request<List<UUID>>.Response response = getRequestFactory().newGetFolderDocumentsRequest(new FoldersEndpoint.FolderRequestParameters(), folder.id).run();
        final Set<UUID> actualDeletedDocIds = new HashSet<>(response.resource);

        // THEN we have received the documents in that folder
        Comparator<UUID> comparator = new Comparator<UUID>() {
            @Override
            public int compare(UUID lhs, UUID rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDocIds, actualDocIds, comparator);
    }

    @SmallTest
    public void test_postDocumentsToFolder_usesCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId.toString()).appendPath("documents").build();
        final Uri actualUrl = getRequestFactory().newPostDocumentToFolderRequest(folderId, UUID.fromString("d0c94e67-0001-0000-0000-000000000000")).getUrl();

        assertEquals("Post document to folder url is wrong", expectedUrl, actualUrl);
    }

    @LargeTest
    public void test_postDocumentsToFolder_createsDocumentsInTheFolderInServer() throws Exception {

        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents
        List<Document> documents = new LinkedList<Document>();
        final Set<UUID> expectedDocIds = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            documents.add(document);
            expectedDocIds.add(document.id);
        }

        // WHEN posting the documents to the folder
        for (Document document : documents) {
            getRequestFactory().newPostDocumentToFolderRequest(folder.id, document.id).run();
        }

        Request<List<UUID>>.Response response = getRequestFactory().newGetFolderDocumentsRequest(new FoldersEndpoint.FolderRequestParameters(), folder.id).run();
        final Set<UUID> actualDocIds = new HashSet<>(response.resource);

        Comparator<UUID> comparator = new Comparator<UUID>() {
            @Override
            public int compare(UUID lhs, UUID rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDocIds, actualDocIds, comparator);
    }

    @SmallTest
    public void test_getDeleteDocumentFromFolder_usesTheCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");
        final UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(MENDELEY_API_BASE_URL).buildUpon().
                appendPath("folders").appendPath(folderId.toString()).
                appendPath("documents").appendPath(documentId.toString()).
                build();
        final Uri actualUrl = getRequestFactory().newDeleteDocumentFromFolderRequest(folderId, documentId).getUrl();

        assertEquals("Delete document from folder url is wrong", expectedUrl, actualUrl);
    }

    @LargeTest
    public void test_deleteDocumentFromFolder_deletesDocumentsInTheFolderInServer() throws Exception {
        // GIVEN a folder
        final Folder folder = getTestAccountSetupUtils().setupFolder(createParentFolder());

        // AND documents in it
        List<Document> documents = new LinkedList<Document>();
        final Set<UUID> expectedDocIds = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title" + i));
            documents.add(document);
            expectedDocIds.add(document.id);
            getRequestFactory().newPostDocumentToFolderRequest(folder.id, document.id).run();
        }

        // WHEN deleting one of the documents
        final UUID deletingDocumentId = documents.remove(getRandom().nextInt(documents.size() -1)).id;
        expectedDocIds.remove(deletingDocumentId);

        getRequestFactory().newDeleteDocumentFromFolderRequest(folder.id, deletingDocumentId).run();

        // THEN the server has no longer that document in the folder
        Request<List<UUID>>.Response response = getRequestFactory().newGetFolderDocumentsRequest(new FoldersEndpoint.FolderRequestParameters(), folder.id).run();
        final Set<UUID> actualDocIds = new HashSet<>(response.resource);

        Comparator<UUID> comparator = new Comparator<UUID>() {
            @Override
            public int compare(UUID lhs, UUID rhs) {
                return lhs.compareTo(rhs);
            }
        };
        AssertUtils.assertSameElementsInCollection(expectedDocIds, actualDocIds, comparator);
    }

    @SmallTest
    public void test_GetFolderRequest_usesCorrectUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID folderId = UUID.fromString("201de700-0001-0000-0000-000000000000");

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("folders").appendPath(folderId.toString()).build();
        final Uri actualUrl = getRequestFactory().newGetFolderRequest(folderId).getUrl();

        assertEquals("Get folder url is wrong", expectedUrl, actualUrl);
    }

    @LargeTest
    public void test_getFolder_receivesTheCorrectFolder() throws Exception {

        // GIVEN a folder that has been posted
        final Folder postingFolder = createParentFolder();
        final UUID folderId = getTestAccountSetupUtils().setupFolder(postingFolder).id;

        // WHEN getting that folder
        final Folder actualFolder =  getRequestFactory().newGetFolderRequest(folderId).run().resource;

        // THEN the folder is the correct one
        AssertUtils.assertFolder(postingFolder, actualFolder);
    }

    private Folder createParentFolder() {
        Folder folder = new Folder.Builder()
                .setName("parent folder" + getRandom().nextInt())
                .build();

        return folder;
    }

    private Folder createSubFolder(UUID parentFolderId) {
        Folder folder = new Folder.Builder()
                .setName("sub folder" + getRandom().nextInt())
                .setParentId(parentFolderId)
                .build();

        return folder;
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
