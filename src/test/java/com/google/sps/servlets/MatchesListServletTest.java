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
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

@RunWith(JUnit4.class)
public class MatchesListServletTest {
  private static final String MATCH_INFO_ENTITY = "match-info";
  private static final String USER_ID_PROPERTY = "id";
  private static final String POTENTIAL_MATCHES_PROPERTY = "potential-matches";
  private static final String FRIENDED_IDS_PROPERTY = "friended-ids";
  private static final String PASSED_IDS_PROPERTY = "passed-ids";
  private static final String MATCHES_LIST_PROPERTY = "matches-list";
  private static final String USER_ID_REQUEST_URL_PARAM = "userid";

  //Test user ids
  private static final String TEST_USER_ID = "5555";
  private static final String TEST_CONNECTION_1_ID = "1776";
  private static final String TEST_CONNECTION_2_ID = "1234";
  private static final String TEST_CONNECTION_3_ID = "9876";

  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock
  private HttpServletRequest mockRequest;
 
  @Mock
  private HttpServletResponse mockResponse;

  private MatchesListServlet servletUnderTest;
  private DatastoreService datastore;
  private StringWriter responseWriter;
  private PrintWriter writer;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);

    helper.setUp();

    servletUnderTest = new MatchesListServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Tests the case in which a user's match list is empty.
   *
   * <p>Should return an empty list.
   */
  @Test
  public void matchInfoWithEmptyList() throws Exception {
    addMatchInfoEntityToDatastore(TEST_USER_ID, /* friendedUsers= */ ImmutableList.of(),
      /* matchedUsers= */ImmutableList.of());
    
    String jsonOutput = execute(TEST_USER_ID);
    List<String> matches = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(matches).isEmpty();
  }

  /**
   * Tests the case where a user has one match in their match list
   *
   * <p>Should return a list with just that one match's id
   */
  @Test
  public void matchInfoWithSingleMatch() throws Exception {
    addMatchInfoEntityToDatastore(TEST_USER_ID, /* friendedUsers= */ ImmutableList.of(TEST_CONNECTION_1_ID),
      /* matchedUsers= */ ImmutableList.of(TEST_CONNECTION_1_ID));
    
    String jsonOutput = execute(TEST_USER_ID);
    List<String> matches = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(matches).containsExactly(TEST_CONNECTION_1_ID);
  }
  
  /**
   * Tests the case where a user has three matches in their match list
   *
   * <p> Should a return a list with the three matches' ids
   */
  @Test
  public void matchInfoWithMultipleMatches() throws Exception {
    addMatchInfoEntityToDatastore(TEST_USER_ID, /* friendedUsers= */ ImmutableList.of(TEST_CONNECTION_1_ID, TEST_CONNECTION_2_ID, TEST_CONNECTION_3_ID),
      /* matchedUsers= */ ImmutableList.of(TEST_CONNECTION_1_ID, TEST_CONNECTION_2_ID, TEST_CONNECTION_3_ID));
    
    String jsonOutput = execute(TEST_USER_ID);
    List<String> matches = Arrays.asList(new Gson().fromJson(jsonOutput, String[].class));

    assertThat(matches).containsExactly(TEST_CONNECTION_1_ID, TEST_CONNECTION_2_ID, TEST_CONNECTION_3_ID);
  }

  /**
   * Method that calls on the MatchInformationServlet and returns the ID of the next
   * potential match for the specified user
   *
   * @param userIDToFetch The ID of the user whose next potential match is being found
   */
  private String execute(String userIDToFetch) throws IOException {
    when(mockRequest.getParameter(USER_ID_REQUEST_URL_PARAM)).thenReturn(userIDToFetch);
    responseWriter = new StringWriter();
    writer = new PrintWriter(responseWriter, true);
    when(mockResponse.getWriter()).thenReturn(writer);

    servletUnderTest.doGet(mockRequest, mockResponse);
    
    return responseWriter.toString();
  }

  private void addMatchInfoEntityToDatastore(String userID, ImmutableList<String> friendedUsers, ImmutableList<String> matchedUsers) {
    Entity newMatchInfoEntity = new Entity(MATCH_INFO_ENTITY);
    newMatchInfoEntity.setProperty(USER_ID_PROPERTY, userID);
    newMatchInfoEntity.setProperty(FRIENDED_IDS_PROPERTY, friendedUsers);
    newMatchInfoEntity.setProperty(MATCHES_LIST_PROPERTY, matchedUsers);
    newMatchInfoEntity.setProperty(POTENTIAL_MATCHES_PROPERTY, ImmutableList.of());
    newMatchInfoEntity.setProperty(PASSED_IDS_PROPERTY, ImmutableList.of());
    datastore.put(newMatchInfoEntity);
  }
}

