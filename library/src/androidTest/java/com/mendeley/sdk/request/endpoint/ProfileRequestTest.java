package com.mendeley.sdk.request.endpoint;

import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

import java.util.UUID;

public class ProfileRequestTest extends SignedInTest {

    /**
     * As we don't have an API to setup profiles for the test,
     * the tests rely on profiles we added manually
     * through the web interface https://www.mendeley.com
     */

    public void test_getProfile_receivesCorrectProfile() throws Exception {
        // GIVEN a profile on the server
        Profile expected = createTestProfile();

        // WHEN getting the profile
        final Profile actual = getRequestFactory().newGetProfileRequest(expected.id).run().resource;

        // THEN we have the expected profile
        AssertUtils.assertProfile(expected, actual);
    }

    public void test_getMeProfile_receivesCorrectMeProfile() throws Exception {
        // GIVEN the user profile on the server
        Profile expected = createTestProfile();

        // WHEN getting the profile
        final Profile actual = getRequestFactory().newGetMyProfileRequest().run().resource;

        // THEN we have the expected profile
        AssertUtils.assertProfile(expected, actual);
    }

    private Profile createTestProfile() {
        Profile profile = new Profile.Builder()
                .setId(UUID.fromString("f38dc0c8-df12-32a0-ae70-28ab4f3409cd"))
                .setFirstName("Mobile")
                .setLastName("Android")
                .build();

        return profile;
    }
}
