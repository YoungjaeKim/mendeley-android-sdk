package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.os.Environment;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.Request;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;
import com.mendeley.sdk.util.DateUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class FileRequestTest extends SignedInTest {

    @SmallTest
    public void test_getFiles_usesRightUrl() throws Exception {

        UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");
        UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        Date addedSince = DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z");
        Date deletedSince = DateUtils.parseMendeleyApiTimestamp("2014-01-21T11:52:30.000Z");

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("files").
                appendQueryParameter("document_id", documentId.toString()).
                appendQueryParameter("group_id", groupId.toString()).
                appendQueryParameter("added_since", DateUtils.formatMendeleyApiTimestamp(addedSince)).
                appendQueryParameter("deleted_since", DateUtils.formatMendeleyApiTimestamp(deletedSince)).
                build();

        FilesEndpoint.FileRequestParameters params = new FilesEndpoint.FileRequestParameters();
        params.documentId = documentId;
        params.groupId = groupId;
        params.addedSince = addedSince;
        params.deletedSince = deletedSince;

        Uri url = getRequestFactory().newGetFilesRequest(params).getUrl();

        assertEquals("Get files url with parameters is wrong", expectedUrl, url);

        expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL + "files");
        params = new FilesEndpoint.FileRequestParameters();
        url = getRequestFactory().newGetFilesRequest(params).getUrl();

        assertEquals("Get files url without parameters is wrong", expectedUrl, url);
    }

    @SmallTest
    public void test_getFile_usesRightUrl() throws Exception {
        final UUID fileId = UUID.fromString("211e0000-0001-0000-0000-000000000000");
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("files").
                appendPath(fileId.toString()).
                build();
        Uri url = getRequestFactory().newGetFileBinaryRequest(fileId, null).getUrl();

        assertEquals("Get file url is wrong", expectedUrl, url);
    }

    @SmallTest
    public void test_getDeletedFile_usesRightUrl() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final UUID fileId = UUID.fromString("211e0000-0001-0000-0000-000000000000");
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("files").
                appendPath(fileId.toString()).
                build();
        Uri url = getRequestFactory().newDeleteFileRequest(fileId).getUrl();

        assertEquals("Delete file url is wrong", expectedUrl, url);
    }

    public void test_getFiles_withoutParameters_receivesCorrectFiles() throws Exception {
        // GIVEN a document with files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files
        final List<File> actual = getRequestFactory().newGetFilesRequest((FilesEndpoint.FileRequestParameters) null).run().resource;

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFiles_withParameters_receivesCorrectFiles() throws Exception {
        // GIVEN a document with files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final Date currentDate = getServerDate();

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files with parameters
        FilesEndpoint.FileRequestParameters params = new FilesEndpoint.FileRequestParameters();
        params.addedSince = currentDate;
        params.documentId = document.id;

        final List<File> actual = getRequestFactory().newGetFilesRequest(params).run().resource;

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getFiles_whenMoreThanOnePage_receivesCorrectFiles() throws Exception {

        // GIVEN a number of files greater than the page size
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        final int pageSize = 3;
        final int pageCount = 2;

        final List<File> expected = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            expected.add(file);
        }

        // WHEN getting files
        final FilesEndpoint.FileRequestParameters params = new FilesEndpoint.FileRequestParameters();
        params.limit = pageSize;

        Request<List<File>>.Response response = getRequestFactory().newGetFilesRequest(params).run();

        final List<File> actual = new LinkedList<File>();
        // THEN we receive a files list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().newGetFilesRequest(response.next).run();
            }
        }

        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.fileHash.compareTo(f2.fileHash);
            }
        };

        // THEN we have the expected files
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }


    public void test_postFile_createsFileInServer() throws Exception {
        // GIVEN a file
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
        String fileName = "android.pdf";
        File postingFile = createFile(document.id);

        // WHEN posting it
        final File returnedFile = getRequestFactory().newPostFileWithBinaryRequest(postingFile.mimeType, document.id, getContext().getAssets().open(fileName), fileName).run().resource;

        // THEN we receive the same file back, with id filled
        AssertUtils.assertFile(postingFile, returnedFile);
        assertNotNull(returnedFile.id);

        // ...and the file exists in the server
        AssertUtils.assertFiles(getRequestFactory().newGetFilesRequest((FilesEndpoint.FileRequestParameters) null).run().resource, Arrays.asList(postingFile));
    }

    public void test_getFileBinary_receivesTheCorrectFile() throws Exception {
        java.io.File downloadedBinaryFile = null;
        try {
            // GIVEN a file
            final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));
            String fileName = "android.pdf";
            File postingFile = createFile(document.id);

            // ...that has been posted
            final File returnedFile = getRequestFactory().newPostFileWithBinaryRequest(postingFile.mimeType, document.id, getContext().getAssets().open(fileName), fileName).run().resource;

            downloadedBinaryFile = new java.io.File(Environment.getExternalStorageDirectory(), "downloadedFile.pdf");

            // WHEN we download it
            long downloaded = getRequestFactory().newGetFileBinaryRequest(returnedFile.id, downloadedBinaryFile).run().resource;

            // THEN we receive correct file
            assertEquals("file length matches", 34355, downloadedBinaryFile.length());
            assertEquals("file length matches", 34355, downloaded);
        } finally {
            if (downloadedBinaryFile != null && downloadedBinaryFile.exists()) {
                downloadedBinaryFile.delete();
            }
        }
    }

    public void test_deleteFile_removesTheFileFromServer() throws Exception {
        // GIVEN some files
        final Document document = getTestAccountSetupUtils().setupDocument(createDocument("doc title"));

        final List<File> serverFilesBefore = new LinkedList<File>();
        String[] fileNames = new String[]{"android.pdf", "api.pdf", "contact.pdf", "google.pdf"};
        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            File file = getTestAccountSetupUtils().setupFile(document.id, fileName, getContext().getAssets().open(fileName));
            serverFilesBefore.add(file);
        }

        // WHEN deleting one of them
        final UUID deletingFileId = serverFilesBefore.get(0).id;
        getRequestFactory().newDeleteFileRequest(deletingFileId).run();;

        // THEN the server does not have the deleted file any more
        final List<File> serverFilesAfter = getRequestFactory().newGetFilesRequest((FilesEndpoint.FileRequestParameters) null).run().resource;
        for (File file : serverFilesAfter) {
            assertFalse(deletingFileId.equals(file.id));
        }
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

    private File createFile(UUID documentId) {
        final File file = new File.Builder()
                .setDocumentId(documentId)
                .setMimeType("application/pdf")
                .build();

        return file;
    }
}
