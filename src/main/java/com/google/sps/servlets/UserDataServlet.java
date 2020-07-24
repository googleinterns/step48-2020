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
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

/**
 * Servlet that provides information about a specific user, and allows setting a user's info.
 * 
 * <p>A User Entity consists of the following information: a user id, name, email, bio, and friends-list.
 * These entities are stored in Datastore with their user id as the 'kind'.
 *
 * <p>TODO(#15): Add Blobstore Keys for each image that the user uploads.
 */

@WebServlet("/user-data")
public class UserDataServlet extends HttpServlet {
  private static final boolean IMAGE_NOT_FOUND = false;
  private static final String DEFAULT_STRING = "";
  private static final String USER_ENTITY = "User";
  private static final String USER_BIO_PROPERTY = "bio";
  private static final String USER_BLOBKEYS_PROPERTY = "blobkeys";
  private static final String USER_EMAIL_PROPERTY = "email";
  private static final String USER_FOUND_PROPERTY = "user-found";
  private static final String USER_FRIENDS_LIST_PROPERTY = "friends-list";
  private static final String USER_ID_PROPERTY = "id";
  private static final String USER_NAME_PROPERTY = "name";
  private static final String USER_PHOTO_1_PROPERTY = "profile-photo";
  private static final String USER_PHOTO_2_PROPERTY = "photo-2";
  private static final String USER_PHOTO_3_PROPERTY = "photo-3";
  private static final String USER_PHOTO_4_PROPERTY = "photo-4";
  private static final String USER_PHOTO_5_PROPERTY = "photo-5";

  private boolean testing = false;
  private BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the userId, and query Datastore for the corresponding entity
    String userId = getStringParameter(request, USER_ID_PROPERTY, DEFAULT_STRING);

    Entity userEntity = datastore.prepare(new Query(USER_ENTITY).setFilter(
        new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId))).asSingleEntity();

    ImmutableMap.Builder<String, Object> userDataBuilder = ImmutableMap.builder();
    if (userEntity == null) {
      // If a user entity was not found
      userDataBuilder.put(USER_FOUND_PROPERTY, false);
    }
    else {
      // If a user entity was found (a single entity)
      // Get the user's information
      userDataBuilder.put(USER_FOUND_PROPERTY, true)
          .put(USER_BIO_PROPERTY, (String) userEntity.getProperty(USER_BIO_PROPERTY))
          .put(USER_EMAIL_PROPERTY, userEntity.getProperty(USER_EMAIL_PROPERTY))
          .put(USER_FRIENDS_LIST_PROPERTY, (ArrayList<String>) userEntity.getProperty(USER_FRIENDS_LIST_PROPERTY))
          .put(USER_ID_PROPERTY, userEntity.getProperty(USER_ID_PROPERTY))
          .put(USER_NAME_PROPERTY, userEntity.getProperty(USER_NAME_PROPERTY))
          .put("blobkeys", (ArrayList<String>) userEntity.getProperty("blobkeys"));
    }

    // Send the user's json data as the response
    response.setContentType("application/json");
    response.getWriter().print(gson.toJson(userDataBuilder.build()));
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
    Entity userEntity = datastore.prepare(new Query(USER_ENTITY).setFilter(
        new FilterPredicate(USER_ID_PROPERTY, FilterOperator.EQUAL, userId))).asSingleEntity();
    if (userEntity == null) {
      // User entity needs to be created, and property values need to be initialized
      userEntity = new Entity(USER_ENTITY);
      userEntity.setProperty(USER_ID_PROPERTY, userId);
      userEntity.setProperty(USER_NAME_PROPERTY, userName);
      userEntity.setProperty(USER_EMAIL_PROPERTY, userEmail);
      userEntity.setProperty(USER_BIO_PROPERTY, userBio);
      userEntity.setProperty(USER_FRIENDS_LIST_PROPERTY, Arrays.asList(friends));
      userEntity.setProperty("blobkeys", new ArrayList<>(Arrays.asList(new String[]{"", "", "", "", ""})));
    }
    else {
      // User entity needs to be updated
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
    getAndStoreBlobKeys(request, userEntity);
    datastore.put(userEntity);

    // Redirect to the profile page, and let the front-end know the current logged in user
    response.sendRedirect("/profile.html?id=" + userId);
  }

  /** Stores the blob-keys (in Datastore) of files uploaded to Blobstore. */
  private void getAndStoreBlobKeys(HttpServletRequest request, Entity userEntity) {
    // Get values determining whether or not to update/save blobkeys of any particular image
    boolean profilePhotoUploaded = getBooleanParameter(request, USER_PHOTO_1_PROPERTY, IMAGE_NOT_FOUND);
    boolean photo2Uploaded = getBooleanParameter(request, USER_PHOTO_2_PROPERTY, IMAGE_NOT_FOUND);
    boolean photo3Uploaded = getBooleanParameter(request, USER_PHOTO_3_PROPERTY, IMAGE_NOT_FOUND);
    boolean photo4Uploaded = getBooleanParameter(request, USER_PHOTO_4_PROPERTY, IMAGE_NOT_FOUND);
    boolean photo5Uploaded = getBooleanParameter(request, USER_PHOTO_5_PROPERTY, IMAGE_NOT_FOUND);

    List<String> blobKeys = (ArrayList<String>) userEntity.getProperty("blobkeys");

    if (profilePhotoUploaded) {
      blobKeys.set(0, getUploadedFileBlobKey(request, USER_PHOTO_1_PROPERTY));
    }
    if (photo2Uploaded) {
      blobKeys.set(1, getUploadedFileBlobKey(request, USER_PHOTO_2_PROPERTY));
    }
    if (photo3Uploaded) {
      blobKeys.set(2, getUploadedFileBlobKey(request, USER_PHOTO_3_PROPERTY));
    }
    if (photo4Uploaded) {
      blobKeys.set(3, getUploadedFileBlobKey(request, USER_PHOTO_4_PROPERTY));
    }
    if (photo5Uploaded) {
      blobKeys.set(4, getUploadedFileBlobKey(request, USER_PHOTO_5_PROPERTY));
    }
  }

  /** Wrap the doGet method for jUnit testing */
  void doGetWrapper(DatastoreService datastoreService, HttpServletRequest request, HttpServletResponse response) throws IOException {
    datastore = datastoreService;
    doGet(request, response);
  }

  /** Wrap the doPost method for jUnit testing */
  void doPostWrapper(DatastoreService datastoreService, HttpServletRequest request, HttpServletResponse response) throws IOException {
    datastore = datastoreService;
    doPost(request, response);
  }

  /** Wrap the doPost method for testing blobstore, for jUnit testing */
  void doPostWrapper(DatastoreService datastoreService, BlobstoreService blobstoreService,
                  HttpServletRequest request, HttpServletResponse response, boolean isTesting) throws IOException {
    testing = isTesting;
    blobstore = blobstoreService;
    datastore = datastoreService;
    doPost(request, response);
  }

  /** Wrap the getUploadedFileBlobKey method to test how Blobstore results are handled, for jUnit testing */
  String getUploadedFileBlobKeyWrapper(HttpServletRequest request, String formInputElementName, BlobstoreService blobstoreService, boolean isTesting) {
    blobstore = blobstoreService;
    testing = isTesting;
    return getUploadedFileBlobKey(request, formInputElementName);
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

  /** Returns the request parameter (for booleans), or the default value if not specified */
  private boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
    String value = request.getParameter(name);
    if (value == null) return false;
    else return value.equals("true");
  }

  /** Gets the blobkey of the image passed to the designated input tag in HTML */
  public String getUploadedFileBlobKey(HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // If user submitted form without selecting a file, then we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    if (!testing) {
      BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
      if (blobInfo.getSize() == 0) {
        blobstore.delete(blobKey);
        return null;
      }
    }
    return blobKey.getKeyString();
  }
}
