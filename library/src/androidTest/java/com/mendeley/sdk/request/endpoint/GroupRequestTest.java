package com.mendeley.sdk.request.endpoint;

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;

import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.UserRole;
import com.mendeley.sdk.Request;
import com.mendeley.sdk.request.SignedInTest;
import com.mendeley.sdk.testUtils.AssertUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class GroupRequestTest extends SignedInTest {

    private static final String[] GROUPS = {
            "Artificial Neural Networks",
            "Polyphasic sleep",
            "Technology of Music"
    };

    private static final UUID[] PROFILE_IDS = {
            UUID.fromString("87777129-2222-3800-9e1c-fa76c68201d7"),
            UUID.fromString("f38dc0c8-df12-32a0-ae70-28ab4f3409cd")
    };

    /**
     * As we don't have an API to setup groups for the test,
     * the tests rely on groups we joined to manually with the account
     * through the web interface https://www.mendeley.com/groups/
     * The same is true with the profile ids used in the getGroupMembers test
     */

    @SmallTest
    public void test_getGroups_usesRightUrl_without_limit() throws Exception {

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("groups").
                build();
        GroupsEndpoint.GroupRequestParameters params = new GroupsEndpoint.GroupRequestParameters();

        Uri actual = getRequestFactory().newGetGroupsRequest(params).getUrl();

        assertEquals("Get groups url without limit is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getGroups_usesRightUrl_with_limit() throws Exception {
        final int limit = 20;

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("groups")
                .appendQueryParameter("limit", String.valueOf(limit)).
                        build();
        GroupsEndpoint.GroupRequestParameters params = new GroupsEndpoint.GroupRequestParameters();
        params.limit = limit;

        Uri actual = getRequestFactory().newGetGroupsRequest(params).getUrl();

        assertEquals("Get groups url with limit is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getGroup_usesRightUrl() throws Exception {

        final UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("groups").
                appendPath(groupId.toString()).
                build();

        Uri actual = getRequestFactory().newGetGroupRequest(groupId).getUrl();

        assertEquals("Get groups url without limit is wrong", expectedUrl, actual);
    }

    @SmallTest
    public void test_getGroupMembers_usesRightUrl() throws Exception {

        final UUID groupId = UUID.fromString("97096000-0001-0000-0000-000000000000");
        final int limit = 30;

        Uri expectedUrl = Uri.parse(Request.MENDELEY_API_BASE_URL).buildUpon().
                appendPath("groups").
                appendPath(groupId.toString()).
                appendPath("members").
                appendQueryParameter("limit", String.valueOf(limit)).
                build();
        GroupsEndpoint.GroupRequestParameters params = new GroupsEndpoint.GroupRequestParameters();
        params.limit = limit;
        Uri actual = getRequestFactory().newGetGroupMembersRequest(params, groupId).getUrl();

        assertEquals("Get groups url without limit is wrong", expectedUrl, actual);
    }

    public void test_getGroups_receivesCorrectGroups() throws Exception {
        // GIVEN some groups on the server
        List<Group> expected = new LinkedList<Group>();
        for (String groupName : GROUPS) {
            expected.add(new Group.Builder().setName(groupName).build());
        }

        // WHEN getting groups
        final List<Group> actual = getRequestFactory().newGetGroupsRequest(new GroupsEndpoint.GroupRequestParameters()).run().resource;

        Comparator<Group> comparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getGroups_whenMoreThanOnePage_receivesCorrectGroups() throws Exception {

        // GIVEN a number of groups on the server greater than the page size
        final int pageSize = 2;
        final int pageCount = 2;

        List<Group> expected = new LinkedList<Group>();
        for (String groupName : GROUPS) {
            expected.add(new Group.Builder().setName(groupName).build());
        }

        // WHEN getting groups
        final GroupsEndpoint.GroupRequestParameters params = new GroupsEndpoint.GroupRequestParameters();
        params.limit = pageSize;

        final List<Group> actual = new LinkedList<Group>();
        Request<List<Group>>.Response response = getRequestFactory().newGetGroupsRequest(params).run();


        // THEN we receive a group list...
        for (int page = 0; page < pageCount; page++) {
            actual.addAll(response.resource);

            //... with a link to the next page if it was not the last page
            if (page < pageCount - 1) {
                assertTrue("page must be valid", response.next != null);
                response = getRequestFactory().newGetGroupsRequest(response.next).run();
            }
        }

        Comparator<Group> comparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, comparator);
    }

    public void test_getGroupById_receivesTheCorrectGroup() throws Exception {
        // GIVEN a group on the server
        Group expected = getTestAccountSetupUtils().getGroups().get(0);

        // WHEN getting the group by id
        final Group actual = getRequestFactory().newGetGroupRequest(expected.id).run().resource;

        // THEN we have the expected group
        AssertUtils.assertGroup(expected, actual);
    }

    public void test_getGroupMembers_receivesTheCorrectGroupMembers() throws Exception {
        // GIVEN a group
        List<Group> groups = getTestAccountSetupUtils().getGroups();

        Comparator<Group> groupComparator = new Comparator<Group>() {
            @Override
            public int compare(Group g1, Group g2) {
                return g1.name.compareTo(g2.name);
            }
        };
        Collections.sort(groups, groupComparator);
        Group group = groups.get(1);

        // AND members of this group
        List<UserRole> expected = new LinkedList<UserRole>();
        for (UUID profileId : PROFILE_IDS) {
            expected.add(new UserRole.Builder().setProfileId(profileId).build());
        }

        // WHEN getting the group members
        List<UserRole> actual = getRequestFactory().newGetGroupMembersRequest(new GroupsEndpoint.GroupRequestParameters(), group.id).run().resource;

        // THEN we have the expected members
        Comparator<UserRole> userRoleComparator = new Comparator<UserRole>() {
            @Override
            public int compare(UserRole u1, UserRole u2) {
                return u1.profileId.compareTo(u2.profileId);
            }
        };

        // THEN we have the expected groups
        AssertUtils.assertSameElementsInCollection(expected, actual, userRoleComparator);
    }
}

