// Copyright 2020 Google LLC
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
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;

/**
 * Servlet that provides information about a specific user, and allows setting a user's info.
 * A User Entity consists of the following information: a user id, name, email, bio, and friends-list.
 * These entities are stored in Datastore with their user id as the 'kind'.
 * After the MVP, user entities will also contain Blobstore Keys for each image that the user has uploaded.
 */

@WebServlet("/user-data")
public class UserDataServlet extends HttpServlet {
  private static final String DEFAULT_STRING = "";
  private static final String USER_ENTITY = "User";
  private static final String USER_BIO_PROPERTY = "bio";
  private static final String USER_EMAIL_PROPERTY = "email";
  private static final String USER_FOUND_PROPERTY = "user-found";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_NAME_PROPERTY = "name";

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the userId, and query Datastore for the corresponding entity
    String userId = getStringParameter(request, USER_ID_PROPERTY, DEFAULT_STRING);
    PreparedQuery results = datastore.prepare(new Query(userId));

    Map<String, Object> userData = new HashMap<>();
    if (results.countEntities() == 0) {
      // If a user entity was not found
      userData.put(USER_FOUND_PROPERTY, false);
    }
    else {
      // If a user entity was found (a single entity)
      Entity userEntity = results.asSingleEntity();

      // Get the user's information
      String name = (String) userEntity.getProperty(USER_NAME_PROPERTY);
      String id = (String) userEntity.getProperty(USER_ID_PROPERTY);
      String email = (String) userEntity.getProperty(USER_EMAIL_PROPERTY);
      String bio = (String) userEntity.getProperty(USER_BIO_PROPERTY);
      ArrayList<String> friendsIds = (ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY);

      userData.put(USER_FOUND_PROPERTY, true);
      userData.put(USER_NAME_PROPERTY, name);
      userData.put(USER_ID_PROPERTY, id);
      userData.put(USER_EMAIL_PROPERTY, email);
      userData.put(USER_BIO_PROPERTY, bio);
      userData.put(USER_FRIENDS_LIST_PROPERTY, friendsIds);
    }

    // Send the user's json data as the response
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(userData));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the user's info to either update or create
    String userId = getStringParameter(request, USER_ID_PROPERTY, DEFAULT_STRING);
    String userName = getStringParameter(request, USER_NAME_PROPERTY, DEFAULT_STRING);
    String userEmail = getStringParameter(request, USER_EMAIL_PROPERTY, DEFAULT_STRING);
    String userBio = getStringParameter(request, USER_BIO_PROPERTY, DEFAULT_STRING);
    String[] friends = getStringArrayParameter(request, USER_FRIENDS_LIST_PROPERTY, new String[]{});

    // Check if a user entity with userId already exists
    PreparedQuery results = datastore.prepare(new Query(userId));
    Entity userEntity = new Entity(USER_ENTITY);
    if (results.countEntities() == 0) {
      // User entity needs to be created
      userEntity = new Entity(userId);
      userEntity.setProperty(USER_ID_PROPERTY, userId);
      userEntity.setProperty(USER_NAME_PROPERTY, userName);
      userEntity.setProperty(USER_EMAIL_PROPERTY, userEmail);
      userEntity.setProperty(USER_BIO_PROPERTY, userBio);
      userEntity.setProperty(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(friends));
    }
    else {
      // User entity needs to be updated
      userEntity = results.asSingleEntity();

      // Set the properties of the user entity without overriding any values with the default string
      setPropertyIfNotDefault(userEntity, USER_ID_PROPERTY, userId, DEFAULT_STRING);
      setPropertyIfNotDefault(userEntity, USER_NAME_PROPERTY, userName, DEFAULT_STRING);
      setPropertyIfNotDefault(userEntity, USER_EMAIL_PROPERTY, userEmail, DEFAULT_STRING);
      setPropertyIfNotDefault(userEntity, USER_BIO_PROPERTY, userBio, DEFAULT_STRING);

      // If the friends-list property wasn't given, don't override the current friends list
      if (friends.length != 0) {
        userEntity.setProperty(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(friends));
      }
    }
    datastore.put(userEntity);

    // Redirect to the profile page, and let the front-end know the current logged in user
    response.sendRedirect("/profile.html?id=" + userId);
  }

  /** Wrap the doGet method for jUnit testing */
  public void doGetWrapper(DatastoreService datastoreService, HttpServletRequest request, HttpServletResponse response) throws IOException {
    datastore = datastoreService;
    doGet(request, response);
  }

  /** Wrap the doPost method for jUnit testing */
  public void doPostWrapper(DatastoreService datastoreService, HttpServletRequest request, HttpServletResponse response) throws IOException {
    datastore = datastoreService;
    doPost(request, response);
  }

  /** Method that sets the entity's value of a particular property, if the value is not the default value */
  private void setPropertyIfNotDefault(Entity entity, String name, String value, String defaultValue) {
    if (!value.equals(defaultValue)) {
      entity.setProperty(name, value);
    }
  }

  /** Returns the request parameter (for Strings), or the default value if not specified */
  private String getStringParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    return value == null ? defaultValue : value;
  }

  /** Returns the request parameter (for String arrays), or the default value if not specified */
  private String[] getStringArrayParameter(HttpServletRequest request, String name, String[] defaultValue) {
    String[] value = request.getParameterValues(name);
    return value == null ? defaultValue : value;
  }
}

