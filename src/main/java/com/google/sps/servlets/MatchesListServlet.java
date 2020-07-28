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
import java.util.Arrays;
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

/**
 * Servlet that handles requests to retrieve a list of a user's matches
 */
@WebServlet("/matches-list")
public class MatchesListServlet extends HttpServlet {
  private static final String MATCH_INFO_ENTITY = "match-info";
  private static final String USER_ID_PROPERTY = "id";
  private static final String POTENTIAL_MATCHES_PROPERTY = "potential-matches";
  private static final String FRIENDED_IDS_PROPERTY = "friended-ids";
  private static final String PASSED_IDS_PROPERTY = "passed-ids";
  private static final String MATCHES_LIST_PROPERTY = "matches-list";
  private static final String USER_ID_REQUEST_URL_PARAM = "userid";
  
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userID = (String) request.getParameter(USER_ID_REQUEST_URL_PARAM);
    
    Entity matchInfoEntity = datastore.prepare(new Query(MATCH_INFO_ENTITY).setFilter(
      new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userID))).asSingleEntity();

    List<String> matchedUsers = (List<String>) matchInfoEntity.getProperty(MATCHES_LIST_PROPERTY);
    if (matchedUsers == null) {
      matchedUsers = Arrays.asList();
    }

    Gson gson = new Gson();
    String json = gson.toJson(matchedUsers);

    response.setContentType("application/json");
    response.getWriter().print(json);
  }
}

