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
import java.util.List;
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
import com.google.gson.Gson;

@RunWith(JUnit4.class)
public class MutualFriendsServletTest {
  private static final String TEST_USER_1_ID = "1111";
  private static final String TEST_USER_2_ID = "1776";
  private static final String TEST_USER_3_ID = "1234";
  private static final String TEST_USER_4_ID = "9876";
  private static final String TEST_USER_5_ID = "5555";

  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock
  private HttpServletRequest mockRequest;
 
  @Mock
  private HttpServletResponse mockResponse;

  private MutualFriendsServlet servletUnderTest;
  private DatastoreService datastore;
  private StringWriter responseWriter;
  private PrintWriter writer;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);

    helper.setUp();

    servletUnderTest = new MutualFriendsServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void noMutualFriendsEmptyFriendsLists() throws Exception {
    addTestUserEntityToDatastore(TEST_USER_1_ID, /* friendsList= */ new String[]{});
    addTestUserEntityToDatastore(TEST_USER_2_ID, /* friendsList= */ new String[]{});

    String jsonOutput = execute(TEST_USER_1_ID, TEST_USER_2_ID);
    List<String> mutualFriends = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(mutualFriends).isEmpty();
  }

  @Test
  public void noMutualFriendsOneEmptyFriendsList() throws Exception {
    addTestUserEntityToDatastore(TEST_USER_1_ID, /* friendsList= */ new String[]{TEST_USER_3_ID, TEST_USER_4_ID});
    addTestUserEntityToDatastore(TEST_USER_2_ID, /* friendsList= */ new String[]{});

    String jsonOutput = execute(TEST_USER_1_ID, TEST_USER_2_ID);
    List<String> mutualFriends = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(mutualFriends).isEmpty();
  }

  @Test
  public void noMutualFriendsFilledLists() throws Exception {
    addTestUserEntityToDatastore(TEST_USER_1_ID, /* friendsList= */ new String[]{TEST_USER_3_ID, TEST_USER_4_ID});
    addTestUserEntityToDatastore(TEST_USER_2_ID, /* friendsList= */ new String[]{TEST_USER_5_ID});

    String jsonOutput = execute(TEST_USER_1_ID, TEST_USER_2_ID);
    List<String> mutualFriends = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(mutualFriends).isEmpty();
  }

  @Test
  public void singleMutualFriend() throws Exception {
    addTestUserEntityToDatastore(TEST_USER_1_ID, /* friendsList= */ new String[]{TEST_USER_3_ID});
    addTestUserEntityToDatastore(TEST_USER_2_ID, /* friendsList= */ new String[]{TEST_USER_3_ID});

    String jsonOutput = execute(TEST_USER_1_ID, TEST_USER_2_ID);
    List<String> mutualFriends = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(mutualFriends).containsExactly(TEST_USER_3_ID);
  }

  @Test
  public void multipleMutualFriends() throws Exception {
    addTestUserEntityToDatastore(TEST_USER_1_ID, /* friendsList= */ new String[]{TEST_USER_3_ID, TEST_USER_4_ID, TEST_USER_5_ID});
    addTestUserEntityToDatastore(TEST_USER_2_ID, /* friendsList= */ new String[]{TEST_USER_3_ID, TEST_USER_4_ID});

    String jsonOutput = execute(TEST_USER_1_ID, TEST_USER_2_ID);
    List<String> mutualFriends = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(mutualFriends).containsExactly(TEST_USER_3_ID, TEST_USER_4_ID);
  }

  /**
   * Method that calls on the MutualFriendsServlet and returns the set of mutual friends
   * between two users.
   *
   * @param userID1 One of the users in the match whose mutual friends are being found
   * @param userID2 The other user in the match whose mutual friends are being found
   * @return The response from the get request to MutualFriendsServlet as a string
   */
  private String execute(String userID1, String userID2) throws IOException {
    when(mockRequest.getParameter(MutualFriendsServlet.USER_ID_1_REQUEST_URL_PARAM)).thenReturn(userID1);
    when(mockRequest.getParameter(MutualFriendsServlet.USER_ID_2_REQUEST_URL_PARAM)).thenReturn(userID2);
    responseWriter = new StringWriter();
    writer = new PrintWriter(responseWriter, true);
    when(mockResponse.getWriter()).thenReturn(writer);

    servletUnderTest.doGet(mockRequest, mockResponse);
    
    return responseWriter.toString();
  }

  private void addTestUserEntityToDatastore(String userID, String[] friendsList) {
    Entity userEntity = new Entity(UserDataServlet.USER_ENTITY);
    userEntity.setProperty(UserDataServlet.USER_ID_PROPERTY, userID);
    userEntity.setProperty(UserDataServlet.USER_FRIENDS_LIST_PROPERTY, Arrays.asList(friendsList));
    userEntity.setProperty(UserDataServlet.USER_NAME_PROPERTY, "");
    userEntity.setProperty(UserDataServlet.USER_EMAIL_PROPERTY, "");
    userEntity.setProperty(UserDataServlet.USER_BIO_PROPERTY, "");
    datastore.put(userEntity);
  }
}

