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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.common.collect.ImmutableSet;
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
*  Handles requests for getting the next potential match for a user's feed page.
*/
@WebServlet("/potential-matches")
public class PotentialMatchesServlet extends HttpServlet {
  private static final String USER_ENTITY = "User";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String NO_POTENTIAL_MATCH_RESULT = "NO_POTENTIAL_MATCHES";
  private static final String USER_ID_REQUEST_URL_PARAM = "userid";
  private static final String MATCH_INFO_ENTITY = "match-info";
  private static final String POTENTIAL_MATCHES_PROPERTY = "potential-matches";
  private static final String FRIENDED_IDS_PROPERTY = "friended-ids";
  private static final String PASSED_IDS_PROPERTY = "passed-ids";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String currUserID = request.getParameter(USER_ID_REQUEST_URL_PARAM);

    loadUserPotentialMatches(currUserID);
    String nextPotentialMatchID = getNextPotentialMatchID(currUserID);

    MatchInformation matchInfo = new MatchInformation(nextPotentialMatchID);
    Gson gson = new Gson();
    String json = gson.toJson(matchInfo);

    response.setContentType("application/json");
    response.getWriter().print(json);
  }

  /**
  * Loads the potential matches list for a specified user.
  *
  * <p>Retrieves match information from datastore or creates new match information
  * entity for user if it had not already been stored.
  *
  * @param userID The userID of the user who's potential match is being found
  * @return The list of potential matches that are found
  */
  private void loadUserPotentialMatches(String userID) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID))).asSingleEntity();
    
    if (matchInfoEntity == null) {
      addMatchInfoToDatastore(userID);
    }
  }
  
  /**
  * Given a specific user, get their next potential match for their feed page
  *
  * @param userID The user whose potential match is being retrieved
  */
  private String getNextPotentialMatchID(String userID) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID))).asSingleEntity();

    List<String> potentialMatches = (ArrayList<String>) matchInfoEntity.getProperty(POTENTIAL_MATCHES_PROPERTY);

    String nextPotentialMatchID;
    
    if (potentialMatches != null && potentialMatches.iterator().hasNext()) {
      nextPotentialMatchID = potentialMatches.iterator().next();

      potentialMatches.remove(nextPotentialMatchID);

      matchInfoEntity.setProperty(POTENTIAL_MATCHES_PROPERTY, potentialMatches);
      datastore.put(matchInfoEntity);
    } else {
      nextPotentialMatchID = NO_POTENTIAL_MATCH_RESULT;
    }
    return nextPotentialMatchID;
  }

  /**
  * Creates a new match-info entity and adds it to the datastore
  *
  * @param userID the user ID of the user whose match information is being stored
  */
  private void addMatchInfoToDatastore(String userID) {
    //Initialize user nodes and friend map
    ImmutableSet<UserNode> userNodes = createUserNodes();
    UserFriendsMap friendsMap = new UserFriendsMap(userNodes);
    
    //Run the potential matching algorithm to find all potential matches
    ImmutableSet<String> potentialMatches = PotentialMatchAlgorithm.findPotentialMatchesForUser(userID, friendsMap);

    //Add new Match Info entity to datastore
    Entity newMatchInfo = new Entity(MATCH_INFO_ENTITY);
    newMatchInfo.setProperty(USER_ID_PROPERTY, userID);
    newMatchInfo.setProperty(POTENTIAL_MATCHES_PROPERTY, new ArrayList<String>(potentialMatches));
    newMatchInfo.setProperty(FRIENDED_IDS_PROPERTY, new ArrayList<String>());
    newMatchInfo.setProperty(PASSED_IDS_PROPERTY, new ArrayList<String>());
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newMatchInfo);
  }

  /**
  * Helper method that creates the set of UserNodes that will be fed into the potential
  * matching method
  *
  * @return the set of user nodes for all current app users
  */
  private ImmutableSet<UserNode> createUserNodes() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(new Query(USER_ENTITY));
    List<Entity> entityResults = results.asList(FetchOptions.Builder.withDefaults());

    ImmutableSet.Builder<UserNode> builder = ImmutableSet.builder();
    for (Entity userEntity: entityResults) {
      String userID = (String) userEntity.getProperty(USER_ID_PROPERTY);
      ArrayList<String> friendsIds =
        (ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY);
      UserNode userNode = new UserNode(userID,
        friendsIds != null ? ImmutableSet.copyOf(friendsIds) : ImmutableSet.of());
      builder.add(userNode);
    }
    
    return builder.build();
  }
}

