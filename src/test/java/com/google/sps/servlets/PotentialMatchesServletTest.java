// Copyright 2019 Google LLC
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.servlets.PotentialMatchesServlet;

@RunWith(JUnit4.class)
public class PotentialMatchesServletTest {
  private static final String NO_POTENTIAL_MATCH_RESULT = "NO_POTENTIAL_MATCHES";
  private static final String MATCHINFO_NEXT_MATCH_ID_FIELD = "nextPotentialMatchID";

  private static final String USER_ENTITY = "User";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_NAME_PROPERTY = "name";
  private static final String USER_EMAIL_PROPERTY = "email";
  private static final String USER_BIO_PROPERTY = "bio";

  private static final String TEST_USER_1_NAME = "Daenerys";
  private static final String TEST_USER_1_ID = "5555";
  private static final String TEST_USER_1_EMAIL = "targaryen@gmail.com";
  private static final String TEST_USER_1_BIO = "dragons <3";

  private static final String TEST_USER_2_NAME = "Eliza";
  private static final String TEST_USER_2_ID = "1776";
  private static final String TEST_USER_2_EMAIL = "elizaHam@gmail.com";
  private static final String TEST_USER_2_BIO = "hamilFam";

  private static final String TEST_USER_3_NAME = "Rory";
  private static final String TEST_USER_3_ID = "1234";
  private static final String TEST_USER_3_EMAIL = "gilmore@gmail.com";
  private static final String TEST_USER_3_BIO = "Stars Hallow :)";

  private static final String TEST_USER_4_NAME = "Steve";
  private static final String TEST_USER_4_ID = "1964";
  private static final String TEST_USER_4_EMAIL = "capAmerica@gmail.com";
  private static final String TEST_USER_4_BIO = "Avengers, assemble";

  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock
  private HttpServletRequest mockRequest;
 
  @Mock
  private HttpServletResponse mockResponse;

  private PotentialMatchesServlet servletUnderTest;
  private DatastoreService datastore;
 
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servletUnderTest = new PotentialMatchesServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
  * Tests scenario where there is only one user in datastore
  *
  * <p>Should result in no next potential match found
  */
  @Test
  public void oneUserNoPotentialMatches() throws Exception {
    when(mockRequest.getParameter("userid")).thenReturn(TEST_USER_1_ID);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, new String[]{});
    
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    String expectedOutput = NO_POTENTIAL_MATCH_RESULT;

    String result = stringWriter.toString();
    JSONObject jsonResponse = new JSONObject(result);
    String actualOutput = jsonResponse.getString(MATCHINFO_NEXT_MATCH_ID_FIELD);

    assertThat(actualOutput).isEqualTo(expectedOutput);
  }

  /**
  * Tests scenario with two users who are friends with each other
  *
  * <p>Should result in no next potential match found
  */
  @Test
  public void twoUsersNoPotentialMatches() throws Exception {
    when(mockRequest.getParameter("userid")).thenReturn(TEST_USER_2_ID);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    String[] testUser1FriendsList = new String[]{TEST_USER_2_ID};
    String[] testUser2FriendsList = new String[]{TEST_USER_1_ID};

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, testUser1FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_2_ID, TEST_USER_2_NAME,
      TEST_USER_2_EMAIL, TEST_USER_2_BIO, testUser1FriendsList);
    
    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    String expectedOutput = NO_POTENTIAL_MATCH_RESULT;

    String result = stringWriter.toString();
    JSONObject jsonResponse = new JSONObject(result);
    String actualOutput = jsonResponse.getString(MATCHINFO_NEXT_MATCH_ID_FIELD);

    assertThat(actualOutput).isEqualTo(expectedOutput);
  }

  /**
  * Tests scenario with three users where User 1 is friends with User 2 and User 3,
  * and User 2 and 3 are only friends with User 1. 
  *
  * <p>Should return that User 2's next potential match is User 3.
  */
  @Test
  public void threeUsersOneMutualConnectionTest() throws Exception {
    when(mockRequest.getParameter("userid")).thenReturn(TEST_USER_2_ID);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    String[] testUser1FriendsList = new String[]{TEST_USER_2_ID, TEST_USER_3_ID};
    String[] testUser2FriendsList = new String[]{TEST_USER_1_ID};
    String[] testUser3FriendsList = new String[]{TEST_USER_1_ID};

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, testUser1FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_2_ID, TEST_USER_2_NAME,
      TEST_USER_2_EMAIL, TEST_USER_2_BIO, testUser2FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_3_ID, TEST_USER_3_NAME,
      TEST_USER_3_EMAIL, TEST_USER_3_BIO, testUser3FriendsList);

    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    String expectedOutput = TEST_USER_3_ID;
    
    String result = stringWriter.toString();
    JSONObject jsonResponse = new JSONObject(result);
    String actualOutput = jsonResponse.getString(MATCHINFO_NEXT_MATCH_ID_FIELD);

    assertThat(actualOutput).isEqualTo(expectedOutput);
  }

  /**
  * Tests scenario with four users where User 1 is friends with User 2, 3, and 4.
  * Users 2, 3, and 4 are only friends with User 1.
  *
  * <p>Should return that User 4's next potential match is either User 2 or 3.
  */
  @Test
  public void fourUsersOneMutualConnectionTest() throws Exception {
    when(mockRequest.getParameter("userid")).thenReturn(TEST_USER_4_ID);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    String[] testUser1FriendsList = new String[]{TEST_USER_2_ID, TEST_USER_3_ID, TEST_USER_4_ID};
    String[] testUser2FriendsList = new String[]{TEST_USER_1_ID};
    String[] testUser3FriendsList = new String[]{TEST_USER_1_ID};
    String[] testUser4FriendsList = new String[]{TEST_USER_1_ID};

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, testUser1FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_2_ID, TEST_USER_2_NAME,
      TEST_USER_2_EMAIL, TEST_USER_2_BIO, testUser2FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_3_ID, TEST_USER_3_NAME,
      TEST_USER_3_EMAIL, TEST_USER_3_BIO, testUser3FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_4_ID, TEST_USER_4_NAME,
      TEST_USER_4_EMAIL, TEST_USER_4_BIO, testUser4FriendsList);

    servletUnderTest.doGetWrapper(datastore, mockRequest, mockResponse);
    writer.flush();

    String result = stringWriter.toString();
    JSONObject jsonResponse = new JSONObject(result);
    String actualOutput = jsonResponse.getString(MATCHINFO_NEXT_MATCH_ID_FIELD);

    assertThat(actualOutput).isIn(Arrays.asList(TEST_USER_2_ID, TEST_USER_3_ID));
  }

  private void addTestUserEntityToDatastore(DatastoreService datastore, String userID, String name, String email, String bio, String[] friendsList) {
    Entity userEntity = new Entity(USER_ENTITY);
    userEntity.setProperty(USER_ID_PROPERTY, userID);
    userEntity.setProperty(USER_NAME_PROPERTY, name);
    userEntity.setProperty(USER_EMAIL_PROPERTY, email);
    userEntity.setProperty(USER_BIO_PROPERTY, bio);
    userEntity.setProperty(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(friendsList));
    datastore.put(userEntity);
  }
}

