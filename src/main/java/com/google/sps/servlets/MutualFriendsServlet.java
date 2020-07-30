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
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Entity;

/**
 * Servlet to handle requests to update match decision information.
 */
@WebServlet("/mutual-friends")
public class MutualFriendsServlet extends HttpServlet {
  private static final String USER_ENTITY = "User";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String USER_ID_1_REQUEST_URL_PARAM = "userid1";
  private static final String USER_ID_2_REQUEST_URL_PARAM = "userid2";

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userID1 = (String) request.getParameter(USER_ID_1_REQUEST_URL_PARAM);
    String userID2 = (String) request.getParameter(USER_ID_2_REQUEST_URL_PARAM);

    Entity userEntity1 = datastore.prepare(new Query(USER_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID1))).asSingleEntity();
    Entity userEntity2 = datastore.prepare(new Query(USER_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID2))).asSingleEntity();

    List<String> userFriendsList1 = (List<String>) userEntity1.getProperty(USER_FRIENDS_LIST_PROPERTY);
    List<String> userFriendsList2 = (List<String>) userEntity2.getProperty(USER_FRIENDS_LIST_PROPERTY);

    Set<String> userFriendsSet1 = userFriendsList1 == null ? ImmutableSet.of() : ImmutableSet.copyOf(userFriendsList1);
    Set<String> userFriendsSet2 = userFriendsList2 == null ? ImmutableSet.of() : ImmutableSet.copyOf(userFriendsList2);

    Set<String> mutualFriendsSet = Sets.intersection(userFriendsSet1, userFriendsSet2);
    List<String> mutualFriendsList = ImmutableList.copyOf(mutualFriendsSet);

    Gson gson = new Gson();
    String json = gson.toJson(mutualFriendsList);

    response.setContentType("application/json");
    response.getWriter().print(json);
  }
}

