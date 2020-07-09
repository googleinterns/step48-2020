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
import com.google.gson.Gson;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
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
  private UserDataServlet servletUnderTest;
  private StringWriter stringWriter;
  private PrintWriter writer;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servletUnderTest = new UserDataServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /** Tests the doGet method, where the datastore doesn't contain the test user id */
  @Test
  public void testGetMethodWithInvalidId() throws Exception {
    // Mock the getParameter call
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    when(mockResponse.getContentType()).thenReturn("application/json");

    // Create a local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Test the doGet method by calling the wrapper method
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    // Build the correct response output
    Map<String, Object> correctResponse = new HashMap<>();
    correctResponse.put(USER_FOUND_PROPERTY, false);

    // Check that the user wasn't found in datastore
    verify(mockRequest, atLeast(1)).getParameter(USER_ID_PROPERTY);
    Assert.assertEquals(gson.toJson(correctResponse), stringWriter.toString().trim());
  }

  /** Tests the doGet method, where datastore does contain the test user id */
  @Test
  public void testGetMethodWithValidId() throws Exception {
    // Mock the getParameter call
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);

    // Store output given by mockResponse
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);
    when(mockResponse.getContentType()).thenReturn("application/json");

    // Create a local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    addTestUserEntityToDatastore(datastore);
    // Test the doGet method by calling the wrapper method
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    // Build the correct response output
    Map<String, Object> correctResponse = new HashMap<>();
    correctResponse.put(USER_FOUND_PROPERTY, true);
    correctResponse.put(USER_NAME_PROPERTY, TEST_USER_NAME);
    correctResponse.put(USER_ID_PROPERTY, TEST_USER_ID);
    correctResponse.put(USER_EMAIL_PROPERTY, TEST_USER_EMAIL);
    correctResponse.put(USER_BIO_PROPERTY, TEST_USER_BIO);
    correctResponse.put(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(TEST_USER_FRIENDS_LIST));

    // Check that the user was found in Datastore
    verify(mockRequest, atLeast(1)).getParameter(USER_ID_PROPERTY);
    Assert.assertEquals(gson.toJson(correctResponse), stringWriter.toString().trim());
  }

  /** Tests the doPost method, making sure it redirects correctly */
  @Test
  public void testPostMethodRedirect() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Create a local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

    // Check that the mockResponse redirected property
    verify(mockRequest, atLeast(1)).getParameter(USER_ID_PROPERTY);
    verify(mockResponse).sendRedirect("/profile.html?id=" + TEST_USER_ID);
  }

  /** Tests the doPost method, making sure that a new user entity is added to Datastore */
  @Test
  public void testPostCreateUserInfo() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Create a local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    PreparedQuery results = datastore.prepare(new Query(TEST_USER_ID));
    Assert.assertEquals(1, results.countEntities());
    Entity userEntity = results.asSingleEntity();

    // Verify that the user entity has the correct properties
    Assert.assertEquals((String) userEntity.getProperty(USER_ID_PROPERTY), TEST_USER_ID);
    Assert.assertEquals((String) userEntity.getProperty(USER_NAME_PROPERTY), TEST_USER_NAME);
    Assert.assertEquals((String) userEntity.getProperty(USER_EMAIL_PROPERTY), TEST_USER_EMAIL);
    Assert.assertEquals((String) userEntity.getProperty(USER_BIO_PROPERTY), TEST_USER_BIO);
    Assert.assertEquals(((ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY)).toArray(),
        TEST_USER_FRIENDS_LIST);
  }

  /** Tests the doPost method, making sure that a user entity is updated in Datastore */
  @Test
  public void testPostUpdateUserInfo() throws Exception {
    // Mock the getParameter and getParameterValues calls
    when(mockRequest.getParameter(USER_ID_PROPERTY)).thenReturn(TEST_USER_ID);
    when(mockRequest.getParameter(USER_NAME_PROPERTY)).thenReturn(ALTERNATE_TEST_USER_NAME);
    when(mockRequest.getParameter(USER_EMAIL_PROPERTY)).thenReturn(TEST_USER_EMAIL);
    when(mockRequest.getParameter(USER_BIO_PROPERTY)).thenReturn(TEST_USER_BIO);
    when(mockRequest.getParameterValues(USER_FRIENDS_LIST_PROPERTY)).thenReturn(TEST_USER_FRIENDS_LIST);

    // Create a local datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    addTestUserEntityToDatastore(datastore);
    // Test the doPost method by calling the wrapper method
    servletUnderTest.doPostWrapper(datastore, mockRequest, mockResponse);

    // Query the local datastore for the newly created user entity
    PreparedQuery results = datastore.prepare(new Query(TEST_USER_ID));
    Assert.assertEquals(1, results.countEntities());
    Entity userEntity = results.asSingleEntity();

    // Verify that the user Entity name property changed
    Assert.assertEquals((String) userEntity.getProperty(USER_ID_PROPERTY), TEST_USER_ID);
    Assert.assertEquals((String) userEntity.getProperty(USER_NAME_PROPERTY), ALTERNATE_TEST_USER_NAME);
    Assert.assertEquals((String) userEntity.getProperty(USER_EMAIL_PROPERTY), TEST_USER_EMAIL);
    Assert.assertEquals((String) userEntity.getProperty(USER_BIO_PROPERTY), TEST_USER_BIO);
    Assert.assertEquals(((ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY)).toArray(),
        TEST_USER_FRIENDS_LIST);
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
