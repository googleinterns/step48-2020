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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Entity;
import org.junit.Test;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

@RunWith(JUnit4.class)
public class MatchDecisionsServletTest {
  private static final String MATCH_INFO_ENTITY = "match-info";
  private static final String USER_ID_PROPERTY = "id";
  private static final String POTENTIAL_MATCHES_PROPERTY = "potential-matches";
  private static final String FRIENDED_IDS_PROPERTY = "friended-ids";
  private static final String PASSED_IDS_PROPERTY = "passed-ids";
  private static final String MATCHES_LIST_PROPERTY = "matches-list";

  private static final String FRIENDED_DECISION = "FRIENDED";
  private static final String PASSED_DECISION = "PASSED";

  private static final String USER_ID_REQUEST_PARAM = "userid";
  private static final String POTENTIAL_MATCH_REQUEST_PARAM = "potentialMatchID";
  private static final String DECISION_REQUEST_PARAM = "decision";

  private static final String TEST_USER_1_ID = "5555";
  private static final String TEST_USER_2_ID = "1776";
  private static final String TEST_USER_3_ID = "1234";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  private DatastoreService datastore;
  private MatchDecisionsServlet servletUnderTest;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    helper.setUp();

    servletUnderTest = new MatchDecisionsServlet();
    datastore = DatastoreServiceFactory.getDatastoreService();
    servletUnderTest.datastore = datastore;

  }

  @After
  public void tearDown() {
    helper.tearDown();
  }
  
  /**
   * Scenario where a user chooses to friend another user as their first decision
   *
   * <p>Should result in the current User 1's friended list getting updated to hold just User 2.
   */
  @Test
  public void firstFriendedDecision() throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(FRIENDED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    addTestMatchInfoToDatastore(datastore, TEST_USER_2_ID, ImmutableList.of(TEST_USER_1_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();

    assertThat(matchInfoEntity).isNotNull();

    List<String> actualFriendedList = (List<String>) matchInfoEntity.getProperty(FRIENDED_IDS_PROPERTY);
    
    assertThat(actualFriendedList).containsExactly(TEST_USER_2_ID);
  }

  /**
   * Scenario where a user chooses to pass on another user as their first decision.
   *
   * <p>Should result in the current User 1's passed list getting updated to hold just User 2.
   */ 
  @Test
  public void firstPassedDecision() throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(PASSED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    addTestMatchInfoToDatastore(datastore, TEST_USER_2_ID, ImmutableList.of(TEST_USER_1_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();

    assertThat(matchInfoEntity).isNotNull();

    List<String> actualPassedList = (List<String>) matchInfoEntity.getProperty(PASSED_IDS_PROPERTY);

    assertThat(actualPassedList).containsExactly(TEST_USER_2_ID);
  }

  /**
   * Tests the case in which the potential match has not yet had their match information stored in datastore.
   * 
   * <p>Should still result in the same behavior as if this potential match had not made any feed decisions
   * yet, which in this case would be the current user's friended list expanding to include the potential match.
   */
  @Test
  public void noMatchInfoForPotentialMatch() throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(FRIENDED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();

    assertThat(matchInfoEntity).isNotNull();

    List<String> actualFriendedList = (List<String>) matchInfoEntity.getProperty(FRIENDED_IDS_PROPERTY);

    assertThat(actualFriendedList).containsExactly(TEST_USER_2_ID);
  }

  /**
   * Scenario where a user friends another user on their feed page resulting in a mutual match.
   *
   * <p>Should result in the matches lists for User 1 and User 2 getting updated to include
   * each other.
   */
  @Test
  public void friendedDecisionMutualMatchFound() throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(FRIENDED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    addTestMatchInfoToDatastore(datastore, TEST_USER_2_ID, ImmutableList.of(),
      ImmutableList.of(TEST_USER_1_ID), ImmutableList.of(), ImmutableList.of());

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity1 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();   
    Entity matchInfoEntity2 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_2_ID))).asSingleEntity();  

    assertThat(matchInfoEntity1).isNotNull();
    assertThat(matchInfoEntity2).isNotNull();

    List<String> actualMatchesList1 = (List<String>) matchInfoEntity1.getProperty(MATCHES_LIST_PROPERTY);
    List<String> actualMatchesList2 = (List<String>) matchInfoEntity2.getProperty(MATCHES_LIST_PROPERTY);

    assertThat(actualMatchesList1).containsExactly(TEST_USER_2_ID);
    assertThat(actualMatchesList2).containsExactly(TEST_USER_1_ID);
  }

  /**
   * Scenario where a user decides to pass on another user who had previously friended them
   *
   * <p>Should result in empty match lists for both users.
   */
  @Test
  public void passedDecisionMutualMatchNotFound() throws IOException{
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(PASSED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
    addTestMatchInfoToDatastore(datastore, TEST_USER_2_ID, ImmutableList.of(),
      ImmutableList.of(TEST_USER_1_ID), ImmutableList.of(), ImmutableList.of());

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity1 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();
    Entity matchInfoEntity2 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_2_ID))).asSingleEntity();

    assertThat(matchInfoEntity1).isNotNull();
    assertThat(matchInfoEntity2).isNotNull();

    List<String> actualMatchesList1 = (List<String>) matchInfoEntity1.getProperty(MATCHES_LIST_PROPERTY);
    List<String> actualMatchesList2 = (List<String>) matchInfoEntity1.getProperty(MATCHES_LIST_PROPERTY);

    assertThat(actualMatchesList1).isEqualTo(null);
    assertThat(actualMatchesList2).isEqualTo(null);
  }

  /**
   * Scenario where a user already has a match, but friends another user which then creates a
   * second match to add to their matches list.
   *
   * <p>Should result in User 1's match list getting updated to include User 2 and 3, and User 2's
   * match list should be updated to include User 1.
   */
  @Test
  public void secondMatchFound() throws IOException {
    when(mockRequest.getParameter(USER_ID_REQUEST_PARAM)).thenReturn(TEST_USER_1_ID);
    when(mockRequest.getParameter(POTENTIAL_MATCH_REQUEST_PARAM)).thenReturn(TEST_USER_2_ID);
    when(mockRequest.getParameter(DECISION_REQUEST_PARAM)).thenReturn(FRIENDED_DECISION);

    addTestMatchInfoToDatastore(datastore, TEST_USER_1_ID, ImmutableList.of(TEST_USER_2_ID),
      ImmutableList.of(TEST_USER_3_ID), ImmutableList.of(), ImmutableList.of(TEST_USER_3_ID));
    addTestMatchInfoToDatastore(datastore, TEST_USER_2_ID, ImmutableList.of(),
      ImmutableList.of(TEST_USER_1_ID), ImmutableList.of(), ImmutableList.of());
    addTestMatchInfoToDatastore(datastore, TEST_USER_3_ID, ImmutableList.of(),
      ImmutableList.of(TEST_USER_1_ID), ImmutableList.of(), ImmutableList.of(TEST_USER_1_ID));

    servletUnderTest.doPost(mockRequest, mockResponse);

    Entity matchInfoEntity1 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_1_ID))).asSingleEntity();
    Entity matchInfoEntity2 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, TEST_USER_2_ID))).asSingleEntity();   

    assertThat(matchInfoEntity1).isNotNull();
    assertThat(matchInfoEntity2).isNotNull();

    List<String> actualMatchesList1 = (List<String>) matchInfoEntity1.getProperty(MATCHES_LIST_PROPERTY);
    List<String> actualMatchesList2 = (List<String>) matchInfoEntity2.getProperty(MATCHES_LIST_PROPERTY);

    assertThat(actualMatchesList1).containsExactly(TEST_USER_2_ID, TEST_USER_3_ID);
    assertThat(actualMatchesList2).containsExactly(TEST_USER_1_ID);
  }

  /**
   * Adds a match information entity to datastore
   *
   * @param datastore The datastore instance the entity is being stored in
   * @param userID The id of the user whose information is being stored
   * @param potentialMatches The list of potential match IDs for thhis particular user
   * @param friendedIDs The list of users who the current user friended on their feed page
   * @param passedIDs The list of users who the current user passed on their feed page
   * @param matchIDs The list of users who the current user matched with
   */
  private void addTestMatchInfoToDatastore(DatastoreService datastore, String userID, List<String> potentialMatches,
                                              List<String> friendedIDs, List<String> passedIDs, List<String> matchIDs) {
    Entity newMatchInfo = new Entity(MATCH_INFO_ENTITY);
    newMatchInfo.setProperty(USER_ID_PROPERTY, userID);
    newMatchInfo.setProperty(POTENTIAL_MATCHES_PROPERTY,potentialMatches);
    newMatchInfo.setProperty(FRIENDED_IDS_PROPERTY, friendedIDs);
    newMatchInfo.setProperty(PASSED_IDS_PROPERTY, passedIDs);
    newMatchInfo.setProperty(MATCHES_LIST_PROPERTY, matchIDs);
    datastore.put(newMatchInfo);
  }
}

