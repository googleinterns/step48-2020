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

package com.google.sps.data;

import static com.google.common.truth.Truth.assertThat;

import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.sps.data.friend_map.*;
import com.google.sps.data.potentialMatchAlgorithm;

/* NOTE: PM = Potential Match .*/

@RunWith(JUnit4.class)
public class potentialMatchAlgorithmTest {

  private static final String USER_A_ID = "12345";
  private static final String USER_B_ID = "23456";
  private static final String USER_C_ID = "34567";
  private static final String USER_D_ID = "45678";
  private static final String USER_E_ID = "56789";

  @Test
  public void oneUserNoFriendsPMTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of());

    Set<UserNode> oneUserNodeSet = ImmutableSet.of(userA);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(oneUserNodeSet);
    ImmutableMap<String, ImmutableSet<String>> resultingPotentialMatches = 
      potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap);
    
    ImmutableMap<String, ImmutableSet<String>> expectedMap = ImmutableMap.of(
      USER_A_ID, ImmutableSet.of()
    );

    assertThat(resultingPotentialMatches).containsExactlyEntriesIn(expectedMap);
  }

  @Test
  public void threeUsersOneFriendshipPMTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of());
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of(USER_C_ID));
    UserNode userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_B_ID));

    Set<UserNode> threeUserNodeSet = ImmutableSet.of(userA, userB, userC);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(threeUserNodeSet);
    ImmutableMap<String, ImmutableSet<String>> resultingPotentialMatches = 
      potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(),
        USER_B_ID, ImmutableSet.of(),
        USER_C_ID, ImmutableSet.of()
      );

    assertThat(resultingPotentialMatches).containsExactlyEntriesIn(expectedMap);
  }

  @Test
  public void threeUsersTwoFriendshipPMTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of(USER_B_ID, USER_C_ID));
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of(USER_A_ID));
    UserNode userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_A_ID));
    
    Set<UserNode> threeUserNodeSet = ImmutableSet.of(userA, userB, userC);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(threeUserNodeSet);
    ImmutableMap<String, ImmutableSet<String>> resultingPotentialMatches = 
      potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(),
        USER_B_ID, ImmutableSet.of(USER_C_ID),
        USER_C_ID, ImmutableSet.of(USER_B_ID)
      );

    assertThat(resultingPotentialMatches).containsExactlyEntriesIn(expectedMap);
  }

  @Test
  public void fiveUsersSeveralConnectionsTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of(USER_B_ID, USER_C_ID, USER_D_ID));
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of(USER_A_ID));
    UserNode userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_A_ID));
    UserNode userD = new UserNode(USER_D_ID, ImmutableSet.of(USER_A_ID, USER_E_ID));
    UserNode userE = new UserNode(USER_E_ID, ImmutableSet.of(USER_D_ID));

    Set<UserNode> fiveUserNodeSet = ImmutableSet.of(userA, userB, userC, userD, userE);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(fiveUserNodeSet);
    ImmutableMap<String, ImmutableSet<String>> resultingPotentialMatches = 
      potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(USER_E_ID),
        USER_B_ID, ImmutableSet.of(USER_C_ID, USER_D_ID),
        USER_C_ID, ImmutableSet.of(USER_B_ID, USER_D_ID),
        USER_D_ID, ImmutableSet.of(USER_B_ID, USER_C_ID),
        USER_E_ID, ImmutableSet.of(USER_A_ID)
      );

    assertThat(resultingPotentialMatches).containsExactlyEntriesIn(expectedMap);
  }

  @Test
  public void threeUsersAllConnectedTest() {
    UserNode userA = new UserNode(USER_A_ID, ImmutableSet.of(USER_B_ID, USER_C_ID));
    UserNode userB = new UserNode(USER_B_ID, ImmutableSet.of(USER_A_ID, USER_C_ID));
    UserNode userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_A_ID, USER_B_ID));

    Set<UserNode> threeUserNodeSet = ImmutableSet.of(userA, userB, userC);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(threeUserNodeSet);
    ImmutableMap<String, ImmutableSet<String>> resultingPotentialMatches = 
      potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap);

    ImmutableMap<String, ImmutableSet<String>> expectedMap =
      ImmutableMap.of(
        USER_A_ID, ImmutableSet.of(),
        USER_B_ID, ImmutableSet.of(),
        USER_C_ID, ImmutableSet.of()
      );
    
    assertThat(resultingPotentialMatches).containsExactlyEntriesIn(expectedMap);
  }
}

