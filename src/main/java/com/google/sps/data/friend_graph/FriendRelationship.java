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
import java.lang.IllegalArgumentException;

/**
* Class to represent a single Facebook friendship between two users.
* Represented by a pair of user IDs.
*/
public class FriendRelationship {
  private String friend1ID;
  private String friend2ID;

  public FriendRelationship(String friend1ID, String friend2ID) {
    if (friend1ID.equals(friend2ID)) {
      throw new IllegalArgumentException("User IDs cannot be the same. Users must be different.");
    }
    this.friend1ID = friend1ID;
    this.friend2ID = friend2ID;
  }

  public String getFriend1ID() {
    return this.friend1ID;
  }

  public String getFriend2ID() {
    return this.friend2ID;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FriendRelationship)) {
      return false;
    }

    FriendRelationship otherFriendship = (FriendRelationship) obj;

    return (this.friend1ID.equals(otherFriendship.getFriend1ID()) && this.friend2ID.equals(otherFriendship.getFriend2ID())) ||
      (this.friend1ID.equals(otherFriendship.getFriend2ID()) && this.friend2ID.equals(otherFriendship.getFriend1ID()));
  }

  @Override
  public int hashCode() {
    return this.friend1ID.hashCode() + this.friend2ID.hashCode();
  }

  @Override
  public String toString() {
    return "Friend 1: " + this.friend1ID + "\n" + "Friend 2: " + this.friend2ID;
  }
} 
