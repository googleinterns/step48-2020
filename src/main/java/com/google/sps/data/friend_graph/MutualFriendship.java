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

/**
* Class to represent a the mutual friends between two users
*/
public class MutualFriendship {

  private String friend1ID;
  private String friend2ID;
  private Set<String> mutualFriends;

  public MutualFriendship(String friend1ID, String friend2ID, Set<String> mutualFriends) {
    this.friend1ID = friend1ID;
    this.friend2ID = friend2ID;
    this.mutualFriends = mutualFriends;
  }

  public String getFriend1() {
    return this.friend1ID;
  }

  public String getFriend2() {
    return this.friend2ID;
  }

  public Set<String> getMutualFriends() {
    return this.mutualFriends;
  }
}
