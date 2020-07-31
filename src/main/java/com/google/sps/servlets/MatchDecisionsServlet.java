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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.google.sps.data.friend_map.UserNode;
import com.google.sps.data.friend_map.UserFriendsMap;
import com.google.sps.data.PotentialMatchAlgorithm;
import com.google.sps.data.MatchInformation;

/**
 * Servlet to handle requests to update match decision information.
 */
@WebServlet("/match-decisions")
public class MatchDecisionsServlet extends HttpServlet {
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
  
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userID = (String) request.getParameter(USER_ID_REQUEST_PARAM);
    String potentialMatchID = (String) request.getParameter(POTENTIAL_MATCH_REQUEST_PARAM);
    String decision = (String) request.getParameter(DECISION_REQUEST_PARAM);

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID))).asSingleEntity();

    // update the current user's match decisions in datastore
    updateMatchDecisionInfo(matchInfoEntity, decision, potentialMatchID);
    // check and update the users' mutual match
    updateMutualMatch(userID, potentialMatchID);
  }

  /**
   * Updates the match decision sets for a user's match information given their feed decision
   * (friended or passed) on a given user.
   *
   * @param userMatchInfo The entity of the user's match information from datastore
   * @param decision Whether the user decided to friend or pass on a user on their feed page
   * @param potentialMatchID The potential match who the user made a decision on
   */
  private void updateMatchDecisionInfo(Entity userMatchInfo, String decision, String potentialMatchID) {
    String decisionsProperty = decision.equals(FRIENDED_DECISION) ? FRIENDED_IDS_PROPERTY : PASSED_IDS_PROPERTY;

    List<String> potentialMatches = (List<String>) userMatchInfo.getProperty(POTENTIAL_MATCHES_PROPERTY);
    potentialMatches.remove(potentialMatchID);

    userMatchInfo.setProperty(POTENTIAL_MATCHES_PROPERTY,
        ImmutableList.copyOf(potentialMatches));

    addItemToDatastoreList(userMatchInfo, decisionsProperty, potentialMatchID);
  }

  /**
   * Checks if the two user's have matched (both had decided to friend each other), and update
   * their match sets in datastore if they did match.
   *
   * @param userID1 The id of one of the users in the potential match
   * @param userID2 The id of the other user in the potential match
   */
  private void updateMutualMatch(String userID1, String userID2) {
    Entity matchInfo1 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID1))).asSingleEntity();
    Entity matchInfo2 = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID2))).asSingleEntity();
    
    if (matchInfo2 != null) {
      List<String> friendedUsers1 = (List<String>) matchInfo1.getProperty(FRIENDED_IDS_PROPERTY);
      List<String> friendedUsers2 = (List<String>) matchInfo2.getProperty(FRIENDED_IDS_PROPERTY);
      if (friendedUsers1 != null && friendedUsers2 != null) {
        if (friendedUsers1.contains(userID2) && friendedUsers2.contains(userID1)) {
          addItemToDatastoreList(matchInfo1, MATCHES_LIST_PROPERTY, userID2);
          addItemToDatastoreList(matchInfo2, MATCHES_LIST_PROPERTY, userID1);
        }
      }
    }
  }

  /**
   * Adds an item to a list stored in datastore with null checks to avoid issues with 
   * empty collections that had been previously stored
   *
   * @param userEntity The entity where the list is being updated
   * @param property The property that is getting updated
   * @param itemToAdd The item that is being added to the given property within the given entity
   */
  private void addItemToDatastoreList(Entity userEntity, String property, String itemToAdd) {
    List<String> propertyList = (List<String>) userEntity.getProperty(property);

    ImmutableList.Builder<String> builder = ImmutableList.builder();
    
    if (propertyList != null) {
      builder.addAll(propertyList);
    }
    builder.add(itemToAdd);
    userEntity.setProperty(property, builder.build());

    datastore.put(userEntity);
  }
}

