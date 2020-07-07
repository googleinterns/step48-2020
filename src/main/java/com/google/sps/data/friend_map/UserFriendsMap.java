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
import com.google.common.collect.ImmutableMap;

/**
* <p>A map of all the direct friendships between users which will be used
* to find a user's set of potential matches (2nd friends-> those who are
* friends with a user's current friend* who is not already friends with the user).
*/
public class UserFriendsMap {

  private ImmutableMap<String, ImmutableSet<String>> friendMap;

  /**
  * Form a map of UserIDs to a set ID's of that user's friends.
  *
  * <p>Map keys are the user IDs for each user and the map values are
  * {@code ImmutableSet<String>} holding the user IDs of their friends.
  *
  * @param userNodes A set of {@code UserNodes} that are used to form the map
  */
  public UserFriendsMap (Set<UserNode> userNodes) {
    this.friendMap = userNodes.stream().collect(
      ImmutableMap.toImmutableMap(UserNode::getID, UserNode::getFriendIDs));

  }

  public ImmutableMap<String, ImmutableSet<String>> getFriendMap() {
    return friendMap;
  }

  public Set<String> getUserIDs() {
    return friendMap.keySet();
  }

  public ImmutableSet<String> getUserFriendIDs(String currentUser) {
    return friendMap.get(currentUser);
  }
}

