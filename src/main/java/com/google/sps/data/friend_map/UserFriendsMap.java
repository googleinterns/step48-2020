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

package com.google.sps.data.friend_map;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import com.google.common.collect.ImmutableSet;

/*
* A map of all the direct friendships between users
*/
public class UserFriendsMap {

  private Map<String, ImmutableSet<String>> friendMap;

  /**
  * Constructor for the UserFriendsMap class
  *
  * Forms a map of UserIDs to a set ID's of that user's friends.
  *
  * Map keys are the UserIDs which are unique identifiers for each user (reprsented
  * as a string here) and the map values are the set of IDs of each friend of the 
  * current user (represented here as an ImmutableSet of Strings).
  * 
  * Map created in this class will be used to find a user's set of potential
  * matches (2nd friends-> those who are friends with a user's current friend
  * who is not already friends with the user).
  *
  * @param userNodes A set of UserNodes that are used to form the graph structure
  */
  public UserFriendsMap (Set<UserNode> userNodes) {
    this.friendMap = new HashMap<>();

    for (UserNode user: userNodes) {
      String userID = user.getUserID();
      ImmutableSet<String> userFriendIDs = user.getCurrentUserFriendIDs();
      friendMap.put(userID, userFriendIDs);
    }
  }

  public Map<String, ImmutableSet<String>> getFriendMap() {
    return this.friendMap;
  }

  public Set<String> getUserIDs() {
    return this.friendMap.keySet();
  }

  public ImmutableSet<String> getUserFriendIDs(String currentUser) {
    return this.friendMap.get(currentUser);
  }
}

