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
import java.util.ArrayList;
import java.util.List;
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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.ImmutableSet;
import com.google.sps.servlets.PotentialMatchesServlet;
import com.google.sps.data.PotentialMatchAlgorithm;
import com.google.sps.data.friend_map.UserFriendsMap;
import com.google.sps.data.friend_map.UserNode;

@RunWith(JUnit4.class)
public class PotentialMatchesServletTest {
  private static final String NO_POTENTIAL_MATCH_RESULT = "NO_POTENTIAL_MATCHES";
  private static final String MATCHINFO_NEXT_MATCH_ID_FIELD = "nextPotentialMatchID";
  private static final String USER_ID_REQUEST_URL_PARAM = "userid";
  private static final String MATCH_INFO_ENTITY = "match-info";
  private static final String POTENTIAL_MATCHES_PROPERTY = "potential-matches";
  private static final String FRIENDED_IDS_PROPERTY = "friended-ids";
  private static final String PASSED_IDS_PROPERTY = "passed-ids";

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
  private StringWriter responseWriter;
  private PrintWriter writer;

  @Before
  public void setUp() throws IOException {
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
  public void oneUserNoMatches() throws Exception {    
    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, new String[]{});
    
    String actualOutput = execute(TEST_USER_1_ID);

    assertThat(actualOutput).isEqualTo(NO_POTENTIAL_MATCH_RESULT);
    datastoreAssertions(TEST_USER_1_ID, null);
  }

  /**
  * Tests scenario with two users who are friends with each other
  *
  * <p>Should result in no next potential match found
  */
  @Test
  public void twoUsersNoMatches() throws Exception {
    String[] testUser1FriendsList = new String[]{TEST_USER_2_ID};
    String[] testUser2FriendsList = new String[]{TEST_USER_1_ID};

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, testUser1FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_2_ID, TEST_USER_2_NAME,
      TEST_USER_2_EMAIL, TEST_USER_2_BIO, testUser1FriendsList);
    
    String actualOutput = execute(TEST_USER_2_ID);
    
    assertThat(actualOutput).isEqualTo(NO_POTENTIAL_MATCH_RESULT);
    datastoreAssertions(TEST_USER_2_ID, null);
  }

  /**
  * Tests scenario with three users where User 1 is friends with User 2 and User 3,
  * and User 2 and 3 are only friends with User 1. 
  *
  * <p>This should return that the first potential match for User 2 is User 3, and
  * then it should show that there is no second potential match.
  */
  @Test
  public void threeUsersOneMutualConnection() throws Exception {
    String[] testUser1FriendsList = new String[]{TEST_USER_2_ID, TEST_USER_3_ID};
    String[] testUser2FriendsList = new String[]{TEST_USER_1_ID};
    String[] testUser3FriendsList = new String[]{TEST_USER_1_ID};

    addTestUserEntityToDatastore(datastore, TEST_USER_1_ID, TEST_USER_1_NAME,
      TEST_USER_1_EMAIL, TEST_USER_1_BIO, testUser1FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_2_ID, TEST_USER_2_NAME,
      TEST_USER_2_EMAIL, TEST_USER_2_BIO, testUser2FriendsList);
    addTestUserEntityToDatastore(datastore, TEST_USER_3_ID, TEST_USER_3_NAME,
      TEST_USER_3_EMAIL, TEST_USER_3_BIO, testUser3FriendsList);
    
    String actualOutput_1 = execute(TEST_USER_2_ID);

    assertThat(actualOutput_1).isEqualTo(TEST_USER_3_ID);
    datastoreAssertions(TEST_USER_2_ID, null);

    String actualOutput_2 = execute(TEST_USER_2_ID);

    assertThat(actualOutput_2).isEqualTo(NO_POTENTIAL_MATCH_RESULT);
    datastoreAssertions(TEST_USER_2_ID, null);
  }

  /**
  * Tests scenario with four users where User 1 is friends with User 2, 3, and 4.
  * Users 2, 3, and 4 are only friends with User 1.
  *
  * <p>This should return that the first potential match for User 4 is either User 2 or 3 and
  * that the second one is the other of the two, and then it should show that there is no
  * third potential match.
  */
  @Test
  public void fourUsersOneMutualConnection() throws Exception {
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

    String actualOutput_1 = execute(TEST_USER_4_ID);

    String expectedSecondOutput = actualOutput_1.equals(TEST_USER_2_ID) ? TEST_USER_3_ID : TEST_USER_2_ID;

    assertThat(actualOutput_1).isIn(Arrays.asList(TEST_USER_2_ID, TEST_USER_3_ID));
    datastoreAssertions(TEST_USER_4_ID, Arrays.asList(expectedSecondOutput));

    String actualOutput_2 = execute(TEST_USER_4_ID);

    assertThat(actualOutput_2).isEqualTo(expectedSecondOutput);
    datastoreAssertions(TEST_USER_4_ID, null);

    String actualOutput_3 = execute(TEST_USER_4_ID);

    assertThat(actualOutput_3).isEqualTo(NO_POTENTIAL_MATCH_RESULT);
    datastoreAssertions(TEST_USER_4_ID, null);
  }

  /**
  * Method that calls on the PotentialMatchesServlet and returns the ID of the next
  * potential match for the specified user
  *
  * @param userIDToFetch The ID of the user whose next potential match is being found
  */
  private String execute(String userIDToFetch) throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_URL_PARAM)).thenReturn(userIDToFetch);
    
    responseWriter = new StringWriter();
    writer = new PrintWriter(responseWriter, true);
    when(mockResponse.getWriter()).thenReturn(writer);

    servletUnderTest.doGet(mockRequest, mockResponse);
    
    String result = responseWriter.toString();
    JSONObject jsonResponse = new JSONObject(result);

    return jsonResponse.getString(MATCHINFO_NEXT_MATCH_ID_FIELD);
  }

  /**
  * Checks if a user's match information was stored in datastore correctly
  *
  * @param currUserID The user ID of the user who's match information is getting checked
  * @param expectedMatches The list of potential matches that are expected to be in datastore
  */
  private void datastoreAssertions(String currUserID, List<String> expectedMatches) {
    Entity userMatchEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, currUserID))).asSingleEntity();
    assertThat(userMatchEntity).isNotNull();
    assertThat((String) userMatchEntity.getProperty(USER_ID_PROPERTY))
      .isEqualTo(currUserID);
    assertThat((ArrayList<String>) userMatchEntity.getProperty(POTENTIAL_MATCHES_PROPERTY))
      .isEqualTo(expectedMatches);
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

