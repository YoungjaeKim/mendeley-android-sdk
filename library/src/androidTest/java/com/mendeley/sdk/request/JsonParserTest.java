package com.mendeley.sdk.request;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.JsonReader;

import com.mendeley.sdk.model.Annotation;
import com.mendeley.sdk.model.Discipline;
import com.mendeley.sdk.model.Document;
import com.mendeley.sdk.model.Education;
import com.mendeley.sdk.model.Employment;
import com.mendeley.sdk.model.File;
import com.mendeley.sdk.model.Folder;
import com.mendeley.sdk.model.Group;
import com.mendeley.sdk.model.Person;
import com.mendeley.sdk.model.Photo;
import com.mendeley.sdk.model.Point;
import com.mendeley.sdk.model.Profile;
import com.mendeley.sdk.model.ReadPosition;
import com.mendeley.sdk.model.UserRole;
import com.mendeley.sdk.util.DateUtils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JsonParserTest extends InstrumentationTestCase {

	final String documentWithNotNullCollectionsFile = "test_document_not_null_collections.json";
    final String documentWithNullCollectionsFile = "test_document_null_collections.json";
    final String folderFile =  "test_folder.json";
    final String fileFile =  "test_file.json";
    final String profileFile =  "test_profile.json";
    final String documentIdsFile =  "test_document_ids.json";
    final String groupFile =  "test_group.json";
    final String userRoleFile =  "test_user_role.json";
    final String annotationWithNotNullValuesFile = "test_annotation_not_null_values.json";
    final String annotationWithNullValuesFile = "test_annotation_null_values.json";
    final String readPositionFile =  "test_read_position.json";

    private Document getTestDocumentWithNonNotNullCollections() throws ParseException {
        HashMap<String,String> identifiers = new HashMap<String, String>();

        Person author = new Person("test-first_name", "test-last_name");
        ArrayList<Person> authorsList = new ArrayList<Person>();
        authorsList.add(author);

        Person editor = new Person("test-first_name", "test-last_name");
        ArrayList<Person>editorsList = new ArrayList<Person>();
        editorsList.add(editor);

        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("test-keyword");

        ArrayList<String> tags = new ArrayList<String>();
        tags.add("test-tag");

        ArrayList<String> websites = new ArrayList<String>();
        websites.add("test-website1");
        websites.add("test-website2");

	    return getTestDocument(authorsList, editorsList, keywords, tags, websites, identifiers);
	}

    private Document getTestDocument(ArrayList<Person> authorsList, ArrayList<Person> editorsList, ArrayList<String> keywords, ArrayList<String> tags, ArrayList<String> websites, HashMap<String, String> identifiers) throws ParseException {
        Document.Builder testDocument = new Document.Builder();

        testDocument.setTitle("test-title");
        testDocument.setType("book");
        testDocument.setAuthors(authorsList);
        testDocument.setEditors(editorsList);
        testDocument.setKeywords(keywords);
        testDocument.setTags(tags);
        testDocument.setWebsites(websites);
        testDocument.setIdentifiers(identifiers);

        testDocument.setLastModified(DateUtils.parseMendeleyApiTimestamp("2014-02-28T11:52:30.000Z"));
        testDocument.setGroupId(UUID.fromString("97096000-0001-0000-0000-000000000000"));
        testDocument.setProfileId(UUID.fromString("670211e0-0001-0000-0000-0000000000000"));
        testDocument.setRead(false);
        testDocument.setStarred(false);
        testDocument.setAuthored(false);
        testDocument.setConfirmed(false);
        testDocument.setHidden(false);
        testDocument.setId(UUID.fromString("d0c94e67-0001-0000-0000-000000000000"));
        testDocument.setMonth(0);
        testDocument.setYear(2014);
        testDocument.setDay(0);
        testDocument.setSource("test-source");
        testDocument.setRevision("test-revision");
        testDocument.setCreated(DateUtils.parseMendeleyApiTimestamp("2014-02-20T16:53:25.000Z"));
        testDocument.setAbstractString("test-abstract");
        testDocument.setPages("1-9");
        testDocument.setVolume("1");
        testDocument.setIssue("1");
        testDocument.setPublisher("test-publisher");
        testDocument.setCity("test-city");
        testDocument.setEdition("1");
        testDocument.setInstitution("test-institution");
        testDocument.setSeries("1");
        testDocument.setChapter("1");
        testDocument.setFileAttached(false);
        testDocument.setFileAttached(false);
        testDocument.setClientData("test-client_data");

        return testDocument.build();
    }

    private Group getTestGroup() throws ParseException {

        Group.Builder testGroup = new Group.Builder();
        testGroup.setName("test-group-name");
        testGroup.setDescription("test-group-description");
        testGroup.setId(UUID.fromString("97096000-0001-0000-0000-000000000000"));
        testGroup.setCreated(DateUtils.parseMendeleyApiTimestamp("2014-07-29T11:22:55.000Z"));
        testGroup.setOwningProfileId(UUID.fromString("670211e0-0001-0000-0000-000000000000"));
        testGroup.setAccessLevel(Group.AccessLevel.PUBLIC);
        testGroup.setRole(Group.Role.OWNER);
        testGroup.setWebpage("test-group-webpage");
        testGroup.setLink("test-group-link");
        Photo testPhoto = new Photo("test-original.png", "test-standard.png", "test-square.png");
        testGroup.setPhoto(testPhoto);
        ArrayList<String> testDisciplines = new ArrayList<String>();
        testDisciplines.add("Computer and Information Science");
        testGroup.setDisciplines(testDisciplines);

        return testGroup.build();
    }

    private UserRole getTestUserRole() {

        UserRole.Builder testUserRole = new UserRole.Builder();
        testUserRole.setProfileId(UUID.fromString("670211e0-0001-0000-0000-000000000000"));
        testUserRole.setJoined("2014-07-29T11:22:55.000Z");
        testUserRole.setRole("owner");

        return testUserRole.build();
    }

    private Folder getTestFolder() throws ParseException {
		Folder.Builder mendeleyFolder = new Folder.Builder();
        mendeleyFolder.setName("test-name");
		mendeleyFolder.setId(UUID.fromString("201de700-0001-0000-0000-000000000000"));
		mendeleyFolder.setAdded(DateUtils.parseMendeleyApiTimestamp("2014-02-20T16:53:25.000Z"));
	    
	    return mendeleyFolder.build();
	}

    private File getTestFile() {
	    File.Builder testFile = new File.Builder();
	    testFile.setId(UUID.fromString("211e0000-0001-0000-0000-000000000000"));
	    testFile.setDocumentId(UUID.fromString("d0c94e67-0001-0000-0000-000000000000"));
	    testFile.setMimeType("test-mime_type");
	    testFile.setFileName("test-file_name");
	    testFile.setFileHash("test-filehash");
        testFile.setFileSize(1024);
	    
	    return testFile.build();
	}

    private Profile getTestProfile() throws ParseException {
		
		Discipline testDiscipline = new Discipline();
		testDiscipline.name = "test-name";
		Photo testPhoto = new Photo("test-original.png", "test-standard.png", "test-square.png");
		Education.Builder testEducation = new Education.Builder();

		testEducation.
                setId(UUID.fromString("ed9ca710-0001-0000-0000-000000000000")).
                setInstitution("test-education_institution").
                setDegree("test-degree").
                setStartDate("2014-12-22").
                setEndDate("2014-12-22").
                setWebsite("www.test.education.website");

        Employment.Builder testEmploymentBuilder = new Employment.Builder();

        testEmploymentBuilder.
                setId(UUID.fromString("e461094e-0001-0000-0000-000000000000")).
                        setInstitution("test-employment_institution").
                        setPosition("test-position").
                        setStartDate("2014-12-22").
                        setEndDate("2014-12-22").
                        setWebsite("www.test.employment.website").
                        setClasses(Arrays.asList("Psychology", "Violin")).
                        setIsMainEmployment(true);

        Profile.Builder testProfile = new Profile.Builder();
		testProfile.setId(UUID.fromString("670211e0-0001-0000-0000-000000000000"));
		testProfile.setFirstName("test-first_name");
		testProfile.setLastName("test-last_name");
		testProfile.setDisplayName("test-display_name");
		testProfile.setEmail("test-email");
		testProfile.setLink("test-link");
		testProfile.setAcademicStatus("test-academic_status");
		testProfile.setVerified(true);
		testProfile.setUserType("test-user_type");
		testProfile.setCreatedAt(DateUtils.parseMendeleyApiTimestamp("2014-04-28T15:37:51.000Z"));
		testProfile.setDiscipline(testDiscipline);
		testProfile.setPhoto(testPhoto);
      	ArrayList<Education> educationList = new ArrayList<Education>();
      	educationList.add(testEducation.build());
		testProfile.setEducation(educationList);
		ArrayList<Employment> employmentList = new ArrayList<Employment>();
		employmentList.add(testEmploymentBuilder.build());
		testProfile.setEmployment(employmentList);

	    return testProfile.build();
	}

    private Annotation getTestAnnotationWithNonNotNullValues() throws ParseException {
        ArrayList<Annotation.Position> positions = new ArrayList<Annotation.Position>();
        positions.add(new Annotation.Position(new Point(1, 2), new Point(3, 4), 5));
        Integer color = Color.argb(255, 255, 0, 0);
        Annotation.PrivacyLevel privacyLevel = Annotation.PrivacyLevel.PRIVATE;
        Annotation.Type type = Annotation.Type.HIGHLIGHT;

        return getTestAnnotation(positions, color, privacyLevel, type);
    }

    private Annotation getTestAnnotation(ArrayList<Annotation.Position> positions, Integer color, Annotation.PrivacyLevel privacyLevel, Annotation.Type type) throws ParseException {
        Annotation.Builder bld = new Annotation.Builder();

        bld.setId(UUID.fromString("a6607a71-0001-0000-0000-000000000000"));
        bld.setText("test-text");
        bld.setPositions(positions);
        if (color != null) {
            bld.setColor(color);
        }
        bld.setCreated(DateUtils.parseMendeleyApiTimestamp("2014-02-20T16:53:25.000Z"));
        bld.setDocumentId(UUID.fromString("d0c94e67-0001-0000-0000-000000000000"));
        bld.setFileHash("test-hash");
        bld.setLastModified(DateUtils.parseMendeleyApiTimestamp("2015-02-20T16:53:25.000Z"));
        bld.setPreviousId(UUID.fromString("67e91095-0001-0000-0000-000000000000"));
        bld.setPrivacyLevel(privacyLevel);
        bld.setProfileId(UUID.fromString("670211e0-0001-0000-0000-000000000000"));
        bld.setType(type);

        return bld.build();
    }

    private ReadPosition getTestReadPosition() throws ParseException {
        return new ReadPosition.Builder()
                .setId(UUID.fromString("7ead6051-0001-0000-0000-000000000000"))
                .setFileId(UUID.fromString("211e0000-0001-0000-0000-000000000000"))
                .setPage(69)
                .setVerticalPosition(1969)
                .setDate(DateUtils.parseMendeleyApiTimestamp("2014-02-20T16:53:25.000Z"))
                .build();
    }

	private String getJsonStringFromAssetsFile(String fileNameName) throws IOException {
	    return getAssetsFileAsString(getInstrumentation().getContext().getAssets(), fileNameName);
	}

    private JsonReader getJsonReaderFromAssetsFile(String fileFile) throws IOException {
        final InputStream is = getInstrumentation().getContext().getAssets().open(fileFile);
        return new JsonReader(new InputStreamReader(is));
    }

	@SmallTest
    public void test_parseDocument_withNotNullCollections()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {

        // GIVEN the JSON representation of a document where its collections (authors, editors...) are NOT null
        Document expectedDocument = getTestDocumentWithNonNotNullCollections();

        JsonReader reader = getJsonReaderFromAssetsFile(documentWithNotNullCollectionsFile);

        // WHEN we parse the JSON
        Document actualDocument = JsonParser.documentFromJson(reader);

        // THEN the parsed document matches the expected one
        assertDocumentsAreEqual(expectedDocument, actualDocument);

        // ...AND the collections are NOT null
        assertFalse(actualDocument.authors.isNull());
        assertFalse(actualDocument.editors.isNull());
        assertFalse(actualDocument.websites.isNull());
        assertFalse(actualDocument.tags.isNull());
        assertFalse(actualDocument.keywords.isNull());
        assertFalse(actualDocument.identifiers.isNull());
    }



    @SmallTest
    public void test_parseDocument_withNullCollections()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {

        // GIVEN the JSON representation of a document where its collections (authors, editors...) ARE null
        Document expectedDocument = getTestDocument(null, null, null, null, null, null);
        JsonReader reader = getJsonReaderFromAssetsFile(documentWithNullCollectionsFile);
        // WHEN we parse the JSON
        Document actualDocument = JsonParser.documentFromJson(reader);

        // THEN the parsed document matches the expected one
        assertDocumentsAreEqual(expectedDocument, actualDocument);

        // ...AND the collections are null
        assertTrue(actualDocument.authors.isEmpty());
        assertTrue(actualDocument.authors.isNull());

        assertTrue(actualDocument.editors.isEmpty());
        assertTrue(actualDocument.editors.isNull());

        assertTrue(actualDocument.websites.isEmpty());
        assertTrue(actualDocument.websites.isNull());

        assertTrue(actualDocument.tags.isEmpty());
        assertTrue(actualDocument.tags.isNull());

        assertTrue(actualDocument.keywords.isEmpty());
        assertTrue(actualDocument.keywords.isNull());

        assertTrue(actualDocument.identifiers.isEmpty());
        assertTrue(actualDocument.identifiers.isNull());

    }

	@SmallTest
	public void test_parseFolder()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {
		Folder expectedFolder = getTestFolder();
		JsonReader reader = getJsonReaderFromAssetsFile(folderFile);

		Folder actualFolder = JsonParser.folderFromJson(reader);

        reader.close();

		boolean equal =
				expectedFolder.id.equals(actualFolder.id) &&
                        expectedFolder.name.equals(actualFolder.name) &&
                        expectedFolder.added.equals(actualFolder.added);

		assertTrue("Parsed folder with wrong or missing data", equal);
	}
	
	@SmallTest
	public void test_parseFile()
			throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {

		File expectedFile = getTestFile();
        JsonReader reader = getJsonReaderFromAssetsFile(fileFile);

		File actualFile = JsonParser.fileFromJson(reader);

		boolean equal =
				expectedFile.id.equals(actualFile.id) &&
				expectedFile.documentId.equals(actualFile.documentId) &&
				expectedFile.mimeType.equals(actualFile.mimeType) &&
				expectedFile.fileName.equals(actualFile.fileName) &&
				expectedFile.fileHash.equals(actualFile.fileHash) &&
                expectedFile.fileSize == actualFile.fileSize;

        reader.close();

		assertTrue("Parsed folder with wrong or missing data", equal);
	}



    @SmallTest
	public void test_parseProfile()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {
		Profile expectedProfile = getTestProfile();
		JsonReader reader = getJsonReaderFromAssetsFile(profileFile);

		final Profile actualProfile = JsonParser.profileFromJson(reader);

		boolean equal =
                expectedProfile.id.equals(actualProfile.id) &&
                        expectedProfile.firstName.equals(actualProfile.firstName) &&
                        expectedProfile.lastName.equals(actualProfile.lastName) &&
                        expectedProfile.displayName.equals(actualProfile.displayName) &&
                        expectedProfile.email.equals(actualProfile.email) &&
                        expectedProfile.link.equals(actualProfile.link) &&
                        expectedProfile.academicStatus.equals(actualProfile.academicStatus) &&
                        expectedProfile.verified.equals(actualProfile.verified) &&
                        expectedProfile.userType.equals(actualProfile.userType) &&
                        expectedProfile.createdAt.equals(actualProfile.createdAt) &&
                        expectedProfile.discipline.name.equals(actualProfile.discipline.name) &&

                        expectedProfile.education.get(0).id.equals(actualProfile.education.get(0).id) &&
                        expectedProfile.education.get(0).institution.equals(actualProfile.education.get(0).institution) &&
                        expectedProfile.education.get(0).degree.equals(actualProfile.education.get(0).degree) &&
                        expectedProfile.education.get(0).startDate.equals(actualProfile.education.get(0).startDate) &&
                        expectedProfile.education.get(0).endDate.equals(actualProfile.education.get(0).endDate) &&
                        expectedProfile.education.get(0).website.equals(actualProfile.education.get(0).website) &&

                        expectedProfile.employment.get(0).id.equals(actualProfile.employment.get(0).id) &&
                        expectedProfile.employment.get(0).institution.equals(actualProfile.employment.get(0).institution) &&
                        expectedProfile.employment.get(0).position.equals(actualProfile.employment.get(0).position) &&
                        expectedProfile.employment.get(0).startDate.equals(actualProfile.employment.get(0).startDate) &&
                        expectedProfile.employment.get(0).endDate.equals(actualProfile.employment.get(0).endDate) &&
                        expectedProfile.employment.get(0).website.equals(actualProfile.employment.get(0).website) &&
                        expectedProfile.employment.get(0).isMainEmployment == actualProfile.employment.get(0).isMainEmployment;

        reader.close();

        assertEquals("Employment classes array size not as expected", expectedProfile.employment.get(0).classes.size(), actualProfile.employment.get(0).classes.size());

        for (int i = 0; i < expectedProfile.employment.get(0).classes.size(); i++) {
            assertEquals("Employment class not equals", expectedProfile.employment.get(0).classes.get(i), (actualProfile.employment.get(0).classes.get(i)));
        }


        assertTrue("Parsed profile with wrong or missing data", equal);

        assertPhoto(expectedProfile.photo, actualProfile.photo);
	}

    private void assertPhoto(Photo actualPhoto, Photo expectedPhoto) {
        assertEquals("original photo", actualPhoto.original, expectedPhoto.original);
        assertEquals("standard photo", actualPhoto.standard, expectedPhoto.standard);
        assertEquals("square photo", actualPhoto.square, expectedPhoto.square);
    }


    @SmallTest
	public void test_jsonFromDocument_withNotNullCollections()
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JSONException, ParseException {

        // GIVEN a document where its collections (authors, editors...) are NOT null
        String expectedJson = getJsonStringFromAssetsFile(documentWithNotNullCollectionsFile);
        Document formattingDocument = getTestDocumentWithNonNotNullCollections();

        // WHEN we format it
        String actualJson = JsonParser.documentToJson(formattingDocument).toString();

        // THEN the obtained JSON matches the expected one
        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @SmallTest
    public void test_jsonFromDocument_withNullCollections()
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JSONException, ParseException {

        // GIVEN a document where its collections (authors, editors...) ARE null
        String expectedJson = getJsonStringFromAssetsFile(documentWithNullCollectionsFile);
        Document formattingDocument = getTestDocument(null, null, null, null, null, null);

        // WHEN we format it
        String actualJson = JsonParser.documentToJson(formattingDocument).toString();

        // THEN the obtained JSON matches the expected one
        JSONAssert.assertEquals(expectedJson, actualJson, false);

        // ...AND the collections are empty, non-null objects
        JSONObject jsonObject = new JSONObject(actualJson);
        assertFalse(jsonObject.has("authors"));

        assertFalse(jsonObject.has("editors"));

        assertFalse(jsonObject.has("websites"));

        assertFalse(jsonObject.has("identifiers"));

        assertFalse(jsonObject.has("tags"));

        assertFalse(jsonObject.has("keywords"));
    }


	@SmallTest
	public void test_jsonFromFolder()
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JSONException, ParseException {
		Folder parsingFolder = getTestFolder();
		
		String actualJson = JsonParser.folderToJson(parsingFolder).toString();
		String expectedJson = getJsonStringFromAssetsFile(folderFile);

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }
	
	@SmallTest
	public void test_jsonFromDocumentId()
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JSONException {
    	UUID documentId = UUID.fromString("d0c94e67-0001-0000-0000-000000000000");
    	String expectedString = "{\"id\":\"d0c94e67-0001-0000-0000-000000000000\"}";
		
		String actualString = JsonParser.documentIdToJson(documentId).toString();

        JSONAssert.assertEquals(expectedString, actualString, false);
    }
	
	@SmallTest
	public void test_parseDocumentIds()
			throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, JSONException {

        final JsonReader reader = getJsonReaderFromAssetsFile(documentIdsFile);

		List<UUID> expectedList = new ArrayList<>();
        expectedList.add(UUID.fromString("d0c94e67-0001-0000-0000-000000000000"));
        expectedList.add(UUID.fromString("d0c94e67-0002-0000-0000-000000000000"));
        expectedList.add(UUID.fromString("d0c94e67-0003-0000-0000-000000000000"));
		
		final List<UUID> actualList = JsonParser.documentsIdsFromJson(reader);

        assertEquals("Wrong list size", expectedList.size(), actualList.size());
		for (int i = 0; i < actualList.size(); i++) {
			assertEquals("Wrong list item ", actualList.get(i), (expectedList.get(i)));
		}
	}

    @SmallTest
    public void test_parseGroup()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {

        final Group expectedGroup = getTestGroup();
        final JsonReader reader = getJsonReaderFromAssetsFile(groupFile);
        final Group actualGroup = JsonParser.groupFromJson(reader);
        reader.close();

        assertEquals("id", expectedGroup.id, actualGroup.id);
        assertEquals("name", expectedGroup.name, actualGroup.name);
        assertEquals("description", expectedGroup.description, actualGroup.description);
        assertEquals("created", expectedGroup.created, actualGroup.created);
        assertEquals("owing_profile_id", expectedGroup.owningProfileId, actualGroup.owningProfileId);
        assertEquals("access_level", expectedGroup.accessLevel, actualGroup.accessLevel);
        assertEquals("role", expectedGroup.role, actualGroup.role);
        assertEquals("webpage", expectedGroup.webpage, actualGroup.webpage);
        assertEquals("link", expectedGroup.link, actualGroup.link);
        assertPhoto(actualGroup.photo, expectedGroup.photo);
        assertEquals("disciplines", expectedGroup.disciplines.get(0), actualGroup.disciplines.get(0));
    }

    @SmallTest
    public void test_parseUserRole()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {
        final UserRole expectedUserRole = getTestUserRole();
        final JsonReader reader = getJsonReaderFromAssetsFile(userRoleFile);

        UserRole actualUserRole = JsonParser.userRoleFromJson(reader);

        assertEquals("profile_id", expectedUserRole.profileId, actualUserRole.profileId);
        assertEquals("joined", expectedUserRole.joined, actualUserRole.joined);
        assertEquals("role", expectedUserRole.role, actualUserRole.role);
    }


    @SmallTest
    public void test_parseAnnotation_withNotNullValues()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {

        // GIVEN the JSON representation of an annotation where its values (boxes, color) are NOT null
        final Annotation expectedAnnotation = getTestAnnotationWithNonNotNullValues();
        final JsonReader reader = getJsonReaderFromAssetsFile(annotationWithNotNullValuesFile);

        // WHEN we parse the JSON
        final Annotation actualAnnotation = JsonParser.annotationFromJson(reader);

        // THEN the parsed document matches the expected one
        assertAnnotationsAreEqual(expectedAnnotation, actualAnnotation);

        // ...AND the values are NOT null
        assertFalse(actualAnnotation.positions.isNull());
        assertNotNull(actualAnnotation.color);
        assertNotNull(actualAnnotation.privacyLevel);
        assertNotNull(actualAnnotation.type);
    }

    @SmallTest
    public void test_parseAnnotation_withNullValues()
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException, ParseException {

        // GIVEN the JSON representation of an annotation where its values (boxes, color) are null
        final Annotation expectedAnnotation = getTestAnnotation(null, null, null, null);
        final JsonReader reader = getJsonReaderFromAssetsFile(annotationWithNullValuesFile);

        // WHEN we parse the JSON
        Annotation actualAnnotation = JsonParser.annotationFromJson(reader);

        // THEN the parsed document matches the expected one
        assertAnnotationsAreEqual(expectedAnnotation, actualAnnotation);

        // ...AND the values are not null
        assertTrue(actualAnnotation.positions.isNull());
        assertNull(actualAnnotation.color);
        assertNull(actualAnnotation.privacyLevel);
        assertNull(actualAnnotation.type);
    }

    @SmallTest
    public void test_parseReadPosition() throws Exception {
        final ReadPosition expected = getTestReadPosition();
        final JsonReader reader = getJsonReaderFromAssetsFile(readPositionFile);

        ReadPosition actual = JsonParser.readPositionFromJson(reader);

        Assert.assertEquals("ReadPosition id", expected.id, actual.id);
        Assert.assertEquals("ReadPosition fileId", expected.fileId, actual.fileId);
        Assert.assertEquals("ReadPosition page", expected.page, actual.page);
        Assert.assertEquals("ReadPosition vertical position", expected.verticalPosition, actual.verticalPosition);
        Assert.assertEquals("ReadPosition date", expected.date, actual.date);
    }


    @SmallTest
    public void test_jsonFromFromReadPosition() throws Exception {

        ReadPosition parsingReadPosition = getTestReadPosition();

        String actualJson = JsonParser.readPositionToJson(parsingReadPosition).toString();
        String expectedJson = getJsonStringFromAssetsFile(readPositionFile);

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    private void assertDocumentsAreEqual(Document doc1, Document doc2)
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {

        assertEquals("title", doc1.title, doc2.title);
        assertEquals("year", doc1.year, doc2.year);
        assertEquals("type", doc1.type, doc2.type);
        assertEquals("lastModified", doc1.lastModified, doc2.lastModified);
        assertEquals("groupId", doc1.groupId, doc2.groupId);
        assertEquals("profileId", doc1.profileId, doc2.profileId);
        assertEquals("read", doc1.read, doc2.read);
        assertEquals("starred", doc1.starred, doc2.starred);
        assertEquals("authored", doc1.authored, doc2.authored);
        assertEquals("confirmed", doc1.confirmed, doc2.confirmed);
        assertEquals("hidden", doc1.hidden, doc2.hidden);
        assertEquals("id", doc1.id, doc2.id);
        assertEquals("month", doc1.month, doc2.month);
        assertEquals("year", doc1.year, doc2.year);
        assertEquals("day", doc1.day, doc2.day);
        assertEquals("source", doc1.source, doc2.source);
        assertEquals("revision", doc1.revision, doc2.revision);
        assertEquals("created", doc1.created, doc2.created);

        assertEquals("abstract", doc1.abstractString, doc2.abstractString);
        assertEquals("pages", doc1.pages, doc2.pages);
        assertEquals("volume", doc1.volume, doc2.volume);
        assertEquals("issue", doc1.issue, doc2.issue);
        assertEquals("publisher", doc1.publisher, doc2.publisher);
        assertEquals("city", doc1.city, doc2.city);
        assertEquals("edition", doc1.edition, doc2.edition);
        assertEquals("institution", doc1.institution, doc2.institution);
        assertEquals("series", doc1.series, doc2.series);
        assertEquals("chapter", doc1.chapter, doc2.chapter);
        assertEquals("fileAttached", doc1.fileAttached, doc2.fileAttached);

        assertEquals("identifiers size", doc1.identifiers.size(), doc2.identifiers.size());
        for (String key : doc1.identifiers.keySet()) {
            assertEquals("identifier " + key, doc1.identifiers.get(key), doc1.identifiers.get(key));
        }

        assertEquals("keywords size", doc1.keywords.size(), doc2.keywords.size());
        for (int i = 0; i < doc1.identifiers.size(); i++) {
            assertEquals("keyword " + i, doc1.keywords.get(i), doc2.keywords.get(i));
        }

        assertEquals("tags size", doc1.tags.size(), doc2.tags.size());
        for (int i = 0; i < doc1.tags.size(); i++) {
            assertEquals("tag " + i, doc1.tags.get(i), doc2.tags.get(i));
        }

        assertEquals("websites size", doc1.websites.size(), doc2.websites.size());
        for (int i = 0; i < doc1.websites.size(); i++) {
            assertEquals("website " + i, doc1.websites.get(i), doc2.websites.get(i));
        }

        assertEquals("author size", doc1.authors.size(), doc2.authors.size());
        for (int i = 0; i < doc1.authors.size(); i++) {
            assertEquals("author firstname" + i, doc1.authors.get(i).firstName, doc2.authors.get(i).firstName);
            assertEquals("author lastName" + i, doc1.authors.get(i).lastName, doc2.authors.get(i).lastName);
        }

        assertEquals("editors size", doc1.editors.size(), doc2.editors.size());
        for (int i = 0; i < doc1.editors.size(); i++) {
            assertEquals("editor firstname" + i, doc1.editors.get(i).firstName, doc2.editors.get(i).firstName);
            assertEquals("editor lastName" + i, doc1.editors.get(i).lastName, doc2.editors.get(i).lastName);
        }
    }

    private void assertAnnotationsAreEqual(Annotation anno1, Annotation anno2)
            throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, JSONException {

        assertEquals("color", anno1.color, anno2.color);
        assertEquals("created", anno1.created, anno2.created);
        assertEquals("type", anno1.type, anno2.type);
        assertEquals("documentId", anno1.documentId, anno2.documentId);
        assertEquals("fileHash", anno1.fileHash, anno2.fileHash);
        assertEquals("id", anno1.id, anno2.id);
        assertEquals("lastModified", anno1.lastModified, anno2.lastModified);
        assertEquals("previousId", anno1.previousId, anno2.previousId);
        assertEquals("privacyLevel", anno1.privacyLevel, anno2.privacyLevel);
        assertEquals("profileId", anno1.profileId, anno2.profileId);
        assertEquals("hidden", anno1.text, anno2.text);

        assertEquals("positions size", anno1.positions.size(), anno2.positions.size());
        for (int i = 0; i < anno1.positions.size(); i++) {
            assertEquals("position " + i, anno1.positions.get(i), anno2.positions.get(i));
        }

    }

    public String getAssetsFileAsString(AssetManager assetManager, String fileName) throws IOException {
        StringBuilder buf=new StringBuilder();
        InputStream json=assetManager.open(fileName);
        BufferedReader in= new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str);
        }

        in.close();
        return buf.toString();
    }


}
