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
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.base.Preconditions;

/**
* Class to represent a User in the map representing all user friendships
*/
public class UserNode {

  private final String userID;
  private final ImmutableSet<String> friendIDs;

  /**
  * Create a node to represent a user with their set of friends
  *
  * @param userID The unique id number of each user (obtained from Facebook API)
  * @param currentUserFriendIDs Immutable set of all of the IDs of a user's friends
  */
  public UserNode(@Nonnull String userID, @Nonnull Collection<String> friendIDs) {
    this.userID = Preconditions.checkNotNull(userID);
    this.friendIDs = ImmutableSet.copyOf(Preconditions.checkNotNull(friendIDs));
  }

  public String getID() {
    return userID;
  }

  public ImmutableSet<String> getFriendIDs() {
    return friendIDs;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UserNode)) {
      return false;
    }
    UserNode otherUser = (UserNode) obj;
    return userID.equals(otherUser.userID) && friendIDs.equals(otherUser.friendIDs);
  }

  @Override
  public int hashCode() {
    int hashTotal = 0;
    final int prime = 31; // use of prime number to create more unique hash values
    hashTotal += prime * userID.hashCode();
    hashTotal += prime * friendIDs.hashCode();
    return hashTotal;
  }
}

