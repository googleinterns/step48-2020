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
import com.google.common.collect.ImmutableSet;

/**
* Class to represent a User in the map representing all user friendships
*/
public class UserNode {

  private final String userID;
  private ImmutableSet<String> currentUserFriendIDs;

  /**
  * Constructs an instance of a UserNode
  *
  * @param userID The unique id number of each user (obtained from Facebook API)
  * @param currentUserFriendIDs Immutable set of all of the IDs of a user's friends
  */
  public UserNode(String userID, Collection<String> currentUserFriendIDs) {
    this.userID = userID;
    this.currentUserFriendIDs = ImmutableSet.copyOf(currentUserFriendIDs);
  }

  public String getUserID() {
    return this.userID;
  }

  public ImmutableSet<String> getCurrentUserFriendIDs() {
    return this.currentUserFriendIDs;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UserNode)) {
      return false;
    }
    UserNode otherUser = (UserNode) obj;
    return this.userID.equals(otherUser.userID) &&
      this.currentUserFriendIDs.equals(otherUser.currentUserFriendIDs);
  }

  @Override
  public int hashCode() {
    int hashTotal = 0;
    final int prime = 31;
    hashTotal += prime * (this.userID == null ? 0 : this.userID.hashCode());
    hashTotal += prime * (this.currentUserFriendIDs == null ? 0 : this.currentUserFriendIDs.hashCode());
    return hashTotal;
  }
}

