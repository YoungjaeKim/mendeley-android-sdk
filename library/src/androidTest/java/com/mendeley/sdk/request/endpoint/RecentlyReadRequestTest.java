package com.mendeley.sdk.request.endpoint;


import android.net.Uri;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.exceptions.MendeleyException;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class RecentlyReadRequestTest extends SignedInTest {

    @SmallTest
    public void test_getRecentlyRead_usesRightUrl() throws Exception {

        final UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        final UUID fileId = UUID.fromString("211e0000-0001-0000-0000-000000000000");
        final int limit =  getRandom().nextInt();

        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon()
                .appendPath("recently_read")
                .appendQueryParameter("group_id", groupId.toString())
                .appendQueryParameter("file_id", fileId.toString())
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();


        final Uri url = getRequestFactory().newGetRecentlyReadRequest(groupId, fileId, limit).getUrl();

        assertEquals("Request url is wrong", expectedUrl, url);
    }

    @LargeTest
    public void test_getRecentlyRead_receivesCorrectItems() throws Exception {
        // GIVEN some documents with files and recently read data existing in the server
        final List<ReadPosition> expected = new LinkedList<ReadPosition>();
        for (int i = 0; i < 5; i++) {
            // add them in reverse order, as that's how the server returns them
            expected.add(0, setupRecentlyReadEntryAndDependancies());
        }

        // WHEN getting recently read positions
        final List<ReadPosition> actual = getRequestFactory().newGetRecentlyReadRequest(null, null, 20).run().resource;

        // THEN we have the expected recently read positions
        AssertUtils.assertReadPositions(expected, actual);
    }

    @SmallTest
    public void test_postRecentlyRead_usesRightUrl() throws Exception {
        final Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().appendPath("recently_read").build();
        final Uri url = getRequestFactory().newPostRecentlyReadRequest(null).getUrl();

        assertEquals("Request url is wrong", expectedUrl, url);
    }

    @LargeTest
    public void test_postRecentlyRead_postsCorrectItems() throws Exception {
        // GIVEN a doc with a file
        final File file = setupDocumentAndFile();

        // WHEN posting a recently read position for it
        final ReadPosition expected = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getRequestFactory().newPostRecentlyReadRequest(expected).run();

        // THEN we have successfully posted it
        final List<ReadPosition> actual = getTestAccountSetupUtils().getAllReadingPositions();
        AssertUtils.assertReadPositions(Arrays.asList(expected), actual);
    }

    @LargeTest
    public void test_postRecentlyRead_postsCorrectItems_whenReadPositionAlreadyExisted() throws Exception {
        // GIVEN a doc with a file
        final File file = setupDocumentAndFile();

        // and a recently read position for it
        final ReadPosition firstReadPostion = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getRequestFactory().newPostRecentlyReadRequest(firstReadPostion).run();

        // WHEN updating (posting for second time) the read position for the same file

        // and a recently read position for it
        final ReadPosition secondReadPostion = new ReadPosition.Builder()
                .setFileId(file.id)
                .setPage(Math.abs(getRandom().nextInt(1000)))
                .setVerticalPosition(Math.abs(getRandom().nextInt(1000)))
                .setDate(new Date())
                .build();

        getRequestFactory().newPostRecentlyReadRequest(secondReadPostion).run();

        // THEN we have successfully posted it
        final List<ReadPosition> actual = getTestAccountSetupUtils().getAllReadingPositions();
        AssertUtils.assertReadPositions(Arrays.asList(secondReadPostion), actual);
    }

    private ReadPosition setupRecentlyReadEntryAndDependancies() throws Exception {
        final UUID fileId = setupDocumentAndFile().id;

        // create recently read position
        int page = getRandom().nextInt();
        int verticalPosition = getRandom().nextInt();

        return getTestAccountSetupUtils().setupReadingPosition(fileId, page, verticalPosition, new Date());
    }

    private File setupDocumentAndFile() throws MendeleyException, IOException {
        // create doc
        final Document doc = new Document.Builder().
                setType("Book").
                setTitle("doc" + getRandom().nextInt()).
                setSource("source" + getRandom().nextInt()).
                build();

        final UUID docId = getTestAccountSetupUtils().setupDocument(doc).id;

        // create file
        final InputStream inputStream = getContext().getAssets().open("android.pdf");
        return getTestAccountSetupUtils().setupFile(docId, "file" + getRandom().nextInt(2000), inputStream);
    }


}
