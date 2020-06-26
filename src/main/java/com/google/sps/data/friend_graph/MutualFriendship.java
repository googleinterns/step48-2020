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


public class MutualFriendship {

  private UserNode friend1;
  private UserNode friend2;
  private Set<UserNode> mutualFriends;

  public MutualFriendship(UserNode friend1, UserNode friend2, Set<UserNode> mutualFriends) {
    this.friend1 = friend1;
    this.friend2 = friend2;
    this.mutualFriends = mutualFriends;
  }

  public UserNode getFriend1() {
    return this.friend1;
  }

  public UserNode getFriend2() {
    return this.friend2;
  }

  public Set<UserNode> getMutualFriends() {
    return this.mutualFriends;
  }
}
