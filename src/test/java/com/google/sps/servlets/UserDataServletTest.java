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
  private static final String USER_BIO_PROPERTY = "bio";
  private static final String USER_EMAIL_PROPERTY = "email";
  private static final String USER_FOUND_PROPERTY = "user-found";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_NAME_PROPERTY = "name";

  // Test User info
  private static final String TEST_USER_NAME = "Tim";
  private static final String TEST_USER_ID = "123";
  private static final String TEST_USER_EMAIL = "tim@gmail.com";
  private static final String TEST_USER_BIO = "Amazing!";
  private static final String[] TEST_USER_FRIENDS_LIST = new String[]{"321"};
  private static final String ALTERNATE_TEST_USER_NAME = "John";

  private final Gson gson = new Gson();
  // Uses a local datastore stored in memory for tests
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private DatastoreService datastore;
  private UserDataServlet servletUnderTest;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servletUnderTest = new UserDataServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
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
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    // Test the doGet method by calling the wrapper method
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    // Build the correct response output
    ImmutableMap<String, Object> expected = ImmutableMap.of(
        USER_FOUND_PROPERTY, false);

    // Check that the user wasn't found in datastore
    assertThat(gson.toJson(expected)).contains(stringWriter.toString());
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
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    addTestUserEntityToDatastore(datastore);
    // Test the doGet method by calling the wrapper method
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    // Build the correct response output
    ImmutableMap.Builder<String, Object> expected = ImmutableMap.builder();
    expected.put(USER_FOUND_PROPERTY, true);
    expected.put(USER_BIO_PROPERTY, TEST_USER_BIO);
    expected.put(USER_EMAIL_PROPERTY, TEST_USER_EMAIL);
    expected.put(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(TEST_USER_FRIENDS_LIST));
    expected.put(USER_ID_PROPERTY, TEST_USER_ID);
    expected.put(USER_NAME_PROPERTY, TEST_USER_NAME);

    // Check that the user was found in Datastore
    assertThat(gson.toJson(expected.build())).contains(stringWriter.toString());
  }

  /**
   * Tests the doPost method, making sure it redirects correctly.
   *
   * <p>Expected response: Redirects to the profile page with the user id.
   */
  @Test
  public void testPostMethodRedirect() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

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
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    Entity userEntity = datastore.prepare(new Query(TEST_USER_ID)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that the user entity has the correct properties
    assertThat((String) userEntity.getProperty(USER_ID_PROPERTY)).contains(TEST_USER_ID);
    assertThat((String) userEntity.getProperty(USER_NAME_PROPERTY)).contains(TEST_USER_NAME);
    assertThat((String) userEntity.getProperty(USER_EMAIL_PROPERTY)).contains(TEST_USER_EMAIL);
    assertThat((String) userEntity.getProperty(USER_BIO_PROPERTY)).contains(TEST_USER_BIO);
    assertThat(Arrays.toString(((ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY)).toArray()))
        .contains(Arrays.toString(TEST_USER_FRIENDS_LIST));
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
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(ALTERNATE_TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    addTestUserEntityToDatastore(datastore);
    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    Entity userEntity = datastore.prepare(new Query(TEST_USER_ID)).asSingleEntity();
    assertThat(userEntity).isNotNull();

    // Verify that the user Entity name property changed
    assertThat((String) userEntity.getProperty(USER_ID_PROPERTY)).contains(TEST_USER_ID);
    assertThat((String) userEntity.getProperty(USER_NAME_PROPERTY)).contains(ALTERNATE_TEST_USER_NAME);
  }

  /** Helper method to add a test user to the local datastore */
  private void addTestUserEntityToDatastore(DatastoreService datastore) {
    Entity userEntity = new Entity(TEST_USER_ID);
    userEntity.setProperty(USER_ID_PROPERTY, TEST_USER_ID);
    userEntity.setProperty(USER_NAME_PROPERTY, TEST_USER_NAME);
    userEntity.setProperty(USER_EMAIL_PROPERTY, TEST_USER_EMAIL);
    userEntity.setProperty(USER_BIO_PROPERTY, TEST_USER_BIO);
    userEntity.setProperty(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(TEST_USER_FRIENDS_LIST));
    datastore.put(userEntity);
  }
}

