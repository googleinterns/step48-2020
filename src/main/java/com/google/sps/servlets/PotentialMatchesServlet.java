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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.friend_map.UserNode;
import com.google.sps.data.friend_map.UserFriendsMap;
import com.google.sps.data.potentialMatchAlgorithm;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

/**
* Handles requests for getting the next potential match for a user's feed page.
 */
@WebServlet("/potential-matches")
public class PotentialMatchesServlet extends HttpServlet {
  private static final String USER_ENTITY = "User";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String currUserID = request.getParameter("userid");
    
    String nextPotentialMatchID = loadUserPotentialMatch(currUserID);

    response.setContentType("text/html");
    response.getWriter().print(nextPotentialMatchID);
  }

  /**
  * Loads the next potential match for a user
  *
  * <p>Currently is just loading all the possible matches and obtaining one of them.
  *
  * <p>TODO(#19): Obtain potential match results from datastore instead of running the 
  * potential match finding methods repeatedly.
  *
  * @param userID The userID of the user who's potential match is being found
  * @return The next potential match for a user
  */
  private String loadUserPotentialMatch(String userID) {
    ImmutableSet<UserNode> userNodes = createUserNodes();
    UserFriendsMap friendsMap = new UserFriendsMap(userNodes);
    
    ImmutableSet<String> potentialMatches = potentialMatchAlgorithm.findPotentialMatchesForUser(userID, friendsMap);
  
    String nextPotentialMatchID = potentialMatches.size() > 0 ?
      potentialMatches.iterator().next() : "NO_POTENTIAL_MATCHES";
    
    return nextPotentialMatchID;
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
    List<Entity> entityResults = results.asList(FetchOptions.Builder.withLimit(Integer.MAX_VALUE));

    ImmutableSet.Builder<UserNode> builder = ImmutableSet.builder();
    for (Entity userEntity: entityResults) {
      String userID = (String) userEntity.getProperty(USER_ID_PROPERTY);
      ArrayList<String> friendsIds =
        (ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY);
      UserNode userNode = new UserNode(userID,
        friendsIds != null ? ImmutableSet.copyOf(friendsIds) : ImmutableSet.of());
      builder.add(userNode);
    }

    ImmutableSet<UserNode> allUserNodes = builder.build();
    
    return allUserNodes;
  }

/** Wrap the doGet method for jUnit testing */
  public void doGetWrapper(DatastoreService datastoreService, HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = datastoreService;
    doGet(request, response);
  }
}

