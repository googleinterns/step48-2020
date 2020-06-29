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

package com.google.sps.data.friend_graph;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/*
* A graph of all the friendships between users
*/
public class UserFriendsGraph {

  private Map<String, Set<String>> friendGraph;
  private Set<FriendRelationship> friendshipEdges;

  /**
  * Constructor for the UserFriendsGraph class
  *
  * Forms the graph which is represented by a map of UserIDs to a set
  * ID's of that user's friends and a set of all the direct relationships
  * ('edges') in the graph.
  *
  * @param userNodes A set of UserNodes that are used to form the graph structure
  */
  public UserFriendsGraph (Set<UserNode> userNodes) {
    this.friendGraph = new HashMap<>();
    this.friendshipEdges = new HashSet<>();

    for (UserNode user: userNodes) {
      String userID = user.getUserID();
      if (!friendGraph.containsKey(userID)) {
        friendGraph.put(userID, new HashSet<String>());
      }

      for (String friendID: user.getUserFriends()) {
        friendGraph.get(userID).add(friendID);
        friendshipEdges.add(new FriendRelationship(userID, friendID));
      }
    }
  }

  public Map<String, Set<String>> getFriendGraph() {
    return this.friendGraph;
  }

  public Set<FriendRelationship> getFriendshipEdges() {
    return this.friendshipEdges;
  }

  public Set<String> getUserIDs() {
    return this.friendGraph.keySet();
  }
}

