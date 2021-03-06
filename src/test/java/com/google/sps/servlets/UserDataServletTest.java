// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests the User Data Servlet */
@RunWith(JUnit4.class)
public final class UserDataServletTest {
  // Test User info
  private static final String TEST_USER_NAME = "Tim";
  private static final String TEST_USER_ID = "123";
  private static final String TEST_USER_EMAIL = "tim@gmail.com";
  private static final String TEST_USER_BIO = "Amazing!";
  private static final String TEST_USER_LINK = "facebook.com/tim";
  private static final String[] TEST_USER_FRIENDS_LIST = new String[]{"321"};
  private static final String ALTERNATE_TEST_USER_NAME = "John";
  private static final String TEST_PHOTO_1_BLOBKEY = "abc";
  private static final String TEST_PHOTO_2_BLOBKEY = "def";

  private final Gson gson = new Gson();
  // Uses a local datastore stored in memory for tests
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  @Mock private BlobstoreService blobstore;
  private DatastoreService datastore;
  private UserDataServlet servletUnderTest;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servletUnderTest = new UserDataServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
    servletUnderTest.blobstore = blobstore;
    servletUnderTest.datastore = datastore;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Tests the doGet method, where the datastore doesn't contain the test user id.
   *
   * <p>Expected output: JSON response with "user-found" property as false.
   */
  @Test
  public void testGetMethodWithInvalidId() throws Exception {
    // Mock the getParameter call
    when(mockRequest.getParameter(UserDataServlet.USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    // Test the doGet method with local datastore
    servletUnderTest.doGet(mockRequest, mockResponse);
    writer.flush();

    // Check that the user wasn't found in datastore
    Map<String, Object> actual = gson.fromJson(stringWriter.toString(), Map.class);
    assertThat(actual).containsEntry(UserDataServlet.USER_FOUND_PROPERTY, false);
  }

  /**
   * Tests the doGet method, where datastore does contain the test user id.
   *
   * <p>Expected output: JSON response with "user-found" property as true,
   * and the user properties filled in with the test user properties.
   */
  @Test
  public void testGetMethodWithValidId() throws Exception {
    // Mock the getParameter call
    when(mockRequest.getParameter(UserDataServlet.USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    addTestUserEntityToDatastore(datastore);
    // Test the doGet method with local datastore
    servletUnderTest.doGet(mockRequest, mockResponse);
    writer.flush();

    // Check that the user was found in Datastore
    Map<String, Object> actual = gson.fromJson(stringWriter.toString(), Map.class);
    assertThat(actual).containsEntry(UserDataServlet.USER_FOUND_PROPERTY, true);
    assertThat(actual).containsEntry(UserDataServlet.USER_ID_PROPERTY, TEST_USER_ID);
  }

  /**
   * Tests the doPost method, making sure it redirects correctly.
   *
   * <p>Expected response: Redirects to the profile page with the user id.
   */
  @Test
  public void testPostMethodRedirect() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(UserDataServlet.USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(UserDataServlet.USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(UserDataServlet.USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(UserDataServlet.USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(UserDataServlet.USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Test the doPost method with local datastore
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Check that the mockResponse redirected property
    verify(mockResponse).sendRedirect("/profile.html?id=" + TEST_USER_ID);
  }

  /**
   * Tests the doPost method, making sure that a new user entity is added to Datastore.
   *
   * <p>Expected response: Creates a test user entity and adds it to the local datastore.
   */
  @Test
  public void testPostCreateUserInfo() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(UserDataServlet.USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(UserDataServlet.USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(UserDataServlet.USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(UserDataServlet.USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(UserDataServlet.USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Test the doPost method with local datastore
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    Entity userEntity = datastore.prepare(new Query(UserDataServlet.USER_ENTITY)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that the user entity has the correct properties
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_ID_PROPERTY)).isEqualTo(TEST_USER_ID);
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_NAME_PROPERTY)).isEqualTo(TEST_USER_NAME);
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_EMAIL_PROPERTY)).isEqualTo(TEST_USER_EMAIL);
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_BIO_PROPERTY)).isEqualTo(TEST_USER_BIO);
    assertThat((ArrayList<String>) userEntity.getProperty(UserDataServlet.USER_FRIENDS_LIST_PROPERTY)).containsExactly(TEST_USER_FRIENDS_LIST);
  }

  /**
   * Tests the doPost method, making sure that a user entity is updated in Datastore.
   *
   * <p>Expected response: Updates the user entity already in the local datastore, and
   * changes the user's name from "Tim" to "John".
   */
  @Test
  public void testPostUpdateUserInfo() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(UserDataServlet.USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(UserDataServlet.USER_NAME_PROPERTY)).thenReturn(ALTERNATE_TEST_USER_NAME);
    when(mockRequest.getParameter(UserDataServlet.USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(UserDataServlet.USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(UserDataServlet.USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    addTestUserEntityToDatastore(datastore);
    // Test the doPost method with local datastore
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    Entity userEntity = datastore.prepare(new Query(UserDataServlet.USER_ENTITY)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that the user Entity name property changed
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_ID_PROPERTY)).isEqualTo(TEST_USER_ID);
    assertThat((String) userEntity.getProperty(UserDataServlet.USER_NAME_PROPERTY)).isEqualTo(ALTERNATE_TEST_USER_NAME);
  }

  /**
   * Tests the getUploadedFileBlobKey method, making sure that a null blobkey is returned when no blobkey is found.
   *
   * <p>Expected response: Null value returned, instead of a valid blobkey.
   */
  @Test
  public void testGetUploadedFileBlobKeyReturnsNull() throws Exception {
    // Create map that blobstore returns, and mock the call
    Map<String, List<BlobKey>> map = new HashMap<>();
    when(blobstore.getUploads(mockRequest)).thenReturn(map);

    // Test getUploadedFileBlobKey method with mock blobstore
    String expected = servletUnderTest.getUploadedFileBlobKey(mockRequest, UserDataServlet.USER_PHOTO_1_PROPERTY);

    // Verify that no blobkey was found, null returned
    assertThat(expected).isNull();
  }
  
  /**
   * Tests the getUploadedFileBlobKey method, making sure that the correct blobkey is returned.
   *
   * <p>Expected response: The blobkey that is retrieved from blobstore is returned by the method.
   */
  @Test
  public void testGetUploadedFileBlobKeyReturnsBlobkey() throws Exception {
    // Create map that blobstore returns, and mock the call
    List<BlobKey> keys = new ArrayList<>();
    keys.add(new BlobKey(TEST_PHOTO_1_BLOBKEY));
    Map<String, List<BlobKey>> map = new HashMap<>();
    map.put(UserDataServlet.USER_PHOTO_1_PROPERTY, keys);
    when(blobstore.getUploads(mockRequest)).thenReturn(map);

    // Test getUploadedFileBlobKey method with mock blobstore
    String expected = servletUnderTest.getUploadedFileBlobKey(mockRequest, UserDataServlet.USER_PHOTO_1_PROPERTY);

    // Verify that the correct blobkey was returned
    assertThat(expected).isEqualTo(TEST_PHOTO_1_BLOBKEY);
  }

  /**
   * Tests the doPost method, making sure that datastore ends up storing the correct blobkey.
   *
   * <p>Expected response: The user profile profile photo blobkey gets updated in blobstore.
   */
  @Test
  public void testPostingImageToBlobstore() throws Exception {
    // Create map that blobstore returns, and mock the call
    List<BlobKey> keys = new ArrayList<>();
    keys.add(new BlobKey(TEST_PHOTO_2_BLOBKEY));
    Map<String, List<BlobKey>> map = new HashMap<>();
    map.put(UserDataServlet.USER_PHOTO_1_PROPERTY, keys);
    when(blobstore.getUploads(mockRequest)).thenReturn(map);
    // Mock parameter request, the profile photo is uploaded
    when(mockRequest.getParameter(UserDataServlet.USER_PHOTO_1_PROPERTY)).thenReturn("true");

    // Test the doPost method with local datastore and mock blobstore
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Retrieve userEntity from datastore
    Entity userEntity = datastore.prepare(new Query(UserDataServlet.USER_ENTITY)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that datastore correctly stores the profile photo blobkey
    assertThat(((ArrayList<String>) userEntity.getProperty(UserDataServlet.USER_BLOBKEYS_PROPERTY)).get(0)).isEqualTo(TEST_PHOTO_2_BLOBKEY);
  }

  /**
   * Tests the doPost method, making sure that datastore isn't updated because a photo isn't uploaded.
   *
   * <p>Expected response: The user photo-blobkey in blobstore should be the default value (empty).
   */
  @Test
  public void testPostMethodWithNoImageUploaded() throws Exception {
    // Create map that blobstore returns, and mock the call
    List<BlobKey> keys = new ArrayList<>();
    keys.add(new BlobKey(TEST_PHOTO_1_BLOBKEY));
    Map<String, List<BlobKey>> map = new HashMap<>();
    map.put(UserDataServlet.USER_PHOTO_1_PROPERTY, keys);
    when(blobstore.getUploads(mockRequest)).thenReturn(map);
    // Mock parameter request, the profile photo is uploaded
    when(mockRequest.getParameter(UserDataServlet.USER_PHOTO_1_PROPERTY)).thenReturn("false");

    // Test the doPost method with local datastore and mock blobstore
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Retrieve userEntity from datastore
    Entity userEntity = datastore.prepare(new Query(UserDataServlet.USER_ENTITY)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that datastore doesn't update the value stored in datastore
    assertThat(((ArrayList<String>) userEntity.getProperty(UserDataServlet.USER_BLOBKEYS_PROPERTY)).get(0)).isEqualTo("");
  }


  /** Helper method to add a test user to the local datastore */
  private void addTestUserEntityToDatastore(DatastoreService datastore) {
    Entity userEntity = new Entity(UserDataServlet.USER_ENTITY);
    userEntity.setProperty(UserDataServlet.USER_ID_PROPERTY, TEST_USER_ID);
    userEntity.setProperty(UserDataServlet.USER_NAME_PROPERTY, TEST_USER_NAME);
    userEntity.setProperty(UserDataServlet.USER_EMAIL_PROPERTY, TEST_USER_EMAIL);
    userEntity.setProperty(UserDataServlet.USER_BIO_PROPERTY, TEST_USER_BIO);
    userEntity.setProperty(UserDataServlet.USER_LINK_PROPERTY, TEST_USER_LINK);
    userEntity.setProperty(UserDataServlet.USER_FRIENDS_LIST_PROPERTY, Arrays.asList(TEST_USER_FRIENDS_LIST));
    userEntity.setProperty(UserDataServlet.USER_BLOBKEYS_PROPERTY, new ArrayList<>(Arrays.asList(new String[]{"", "", "", "", ""})));
    datastore.put(userEntity);
  }
}

