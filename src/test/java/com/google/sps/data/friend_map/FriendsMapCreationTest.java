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

import static com.google.common.truth.Truth.assertThat;

import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;

/** */
@RunWith(JUnit4.class)
public final class FriendsMapCreationTest {
  private static final String USER_A_ID = "12345";
  private static final String USER_B_ID = "23456";
  private static final String USER_C_ID = "34567";

  /**
  * Tests if map is forming correctly when there is only one user with 0 friends.
  *
  * <p>Should result in a UserFriendsMap that has a single key that maps
  * to an empty set.
  */
  @Test
  public void oneUserGraphTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of());

    Set<UserNode> oneUserSet = ImmutableSet.of(userA);
    UserFriendsMap oneUserFriendsMap = new UserFriendsMap(oneUserSet);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
          USER_A_ID, ImmutableSet.of()
      );

    assertThat(oneUserFriendsMap.getFriendMap()).containsExactlyEntriesIn(expectedMap);
  }

  /**
  * Tests if map is forming correctly when there are two users with 0 friends.
  *
  * <p>Should result in a UserFriendsMap that has two keys each of which
  * map to an empty set.
  */
  @Test
  public void twoUsersNoConnectionsGraphTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of());
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of());

    Set<UserNode> twoUserSet = ImmutableSet.of(userA, userB);
    UserFriendsMap twoUserFriendsMap = new UserFriendsMap(twoUserSet);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(),
        USER_B_ID, ImmutableSet.of()
      );

    assertThat(twoUserFriendsMap.getFriendMap()).containsExactlyEntriesIn(expectedMap);
  }

  /**
  * Tests if map is forming correctly when there are just 3 users, 2 of which are
  * friends with the third user. There are no connections other than this.
  *
  * <p>Should result in a UserFriendsMap with three keys:
  *    User A should map to a set with just User C
  *    User B should map to a set with just User C
  *    User C should map to a set with User A and User B.
  */
  @Test
  public void threeUsersTwoConnectionGraphTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of(USER_C_ID));
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of(USER_C_ID));
    UserNode userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_A_ID, USER_B_ID));
    
    Set<UserNode> threeUsersTwoConnectionSet = ImmutableSet.of(userA, userB, userC);
    UserFriendsMap threeUsersTwoConnectionMap = new UserFriendsMap(threeUsersTwoConnectionSet);
    
    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(USER_C_ID),
        USER_B_ID, ImmutableSet.of(USER_C_ID),
        USER_C_ID, ImmutableSet.of(USER_A_ID, USER_B_ID)
      );
    
    assertThat(threeUsersTwoConnectionMap.getFriendMap()).containsExactlyEntriesIn(expectedMap);
  }
}

