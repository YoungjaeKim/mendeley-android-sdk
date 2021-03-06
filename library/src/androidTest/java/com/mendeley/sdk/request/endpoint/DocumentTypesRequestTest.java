package com.mendeley.sdk.request.endpoint;

import com.mendeley.sdk.request.SignedInTest;

import java.util.Map;
import java.util.Set;

public class DocumentTypesRequestTest extends SignedInTest {

    public void test_getDocumentTypes_receivesCorrectDocumentTypes() throws Exception {
        final Map<String, String> types = getRequestFactory().newGetDocumentTypesRequest().run().resource;

        Set<String> keys = types.keySet();
        assertTrue("document types must contain journal", keys.contains("journal"));
        assertTrue("document types must contain book", keys.contains("book"));
        //...
    }

}
