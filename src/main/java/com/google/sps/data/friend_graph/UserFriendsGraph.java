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

  private Map<UserNode, Set<UserNode>> friendGraph;
  private Set<UserNode> userNodes;
  private Set<FriendRelationship> friendshipEdges;

  public UserFriendsGraph (Set<UserNode> userNodes) {
    this.userNodes = userNodes;
    this.friendGraph = new HashMap<>();
    this.friendshipEdges = new HashSet<>();

    for (UserNode user: userNodes) {
      if (!friendGraph.containsKey(user)) {
        friendGraph.put(user, new HashSet<UserNode>());
      }

      for (UserNode friend: user.getUserFriends()) {
        friendGraph.get(user).add(friend);
        friendshipEdges.add(new FriendRelationship(user, friend));
      }
    }
  }

  public Map<UserNode, Set<UserNode>> getFriendGraph() {
    return this.friendGraph;
  }
  
  public Set<UserNode> getUserNodes() {
    return this.userNodes;
  }

  public Set<FriendRelationship> getFriendshipEdges() {
    return this.friendshipEdges;
  }
}
