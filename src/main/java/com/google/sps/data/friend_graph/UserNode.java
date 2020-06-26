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

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
* Class to represent a User in the graph data structure representing all user friendships
*/
public class UserNode {

  private String userID;
  private Set<UserNode> userFriends;

  /*
  * Constructs an instance of a UserNode
  *
  * @param userID The unique id number of each user (obtained from Facebook API)
  * @param userFriends Set of all of the user's friends
  */
  public UserNode(String userID, Set<UserNode> userFriends) {
    this.userID = userID;
    this.userFriends = userFriends;
  }

  public String getUserID() {
    return this.userID;
  }

  public Set<UserNode> getUserFriends() {
    return this.userFriends;
  }

  public void updateUserFriends(Set<UserNode> newUserFriends) {
    this.userFriends = newUserFriends;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UserNode)) {
      return false;
    }
    UserNode otherUser = (UserNode) obj;
    return this.userID.equals(otherUser.getUserID());
  }
}
