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

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
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
  private static final String USER_D_ID = "45678";
  private static final String USER_E_ID = "56789";

  private UserNode userA;
  private UserNode userB;
  private UserNode userC;
  private UserNode userD;
  private UserNode userE;

  /**
  * Tests if map is forming correctly when there is only one user with 0 friends.
  *
  * <p>Should result in a UserFriendsMap that has a single key that maps
  * to an empty friend set.
  */
  @Test
  public void oneUserGraphTest() {
    userA = new UserNode(USER_A_ID, ImmutableSet.of());

    Set<UserNode> oneUserSet = new HashSet<>();
    oneUserSet.add(userA);
    UserFriendsMap oneUserFriendsMap = new UserFriendsMap(oneUserSet);

    Map<String, ImmutableSet<String>> expectedMap = new HashMap<>();
    expectedMap.put(USER_A_ID, ImmutableSet.of());

    Assert.assertEquals(oneUserFriendsMap.getFriendMap(), ImmutableMap.copyOf(expectedMap));
  }

  /**
  * Tests if map is forming correctly when there are two users with 0 friends.
  *
  * <p>Should result in a UserFriendsMap that has two keys each of which
  * map to an empty friend set.
  */
  @Test
  public void twoUsersNoConnectionsGraphTest() {
    userA = new UserNode(USER_A_ID, ImmutableSet.of());
    userB = new UserNode(USER_B_ID, ImmutableSet.of());

    Set<UserNode> twoUserSet = new HashSet<>();
    twoUserSet.add(userA);
    twoUserSet.add(userB);
    UserFriendsMap twoUserFriendsMap = new UserFriendsMap(twoUserSet);

    Map<String, ImmutableSet<String>> expectedMap = new HashMap<>();
    expectedMap.put(USER_A_ID, ImmutableSet.of());
    expectedMap.put(USER_B_ID, ImmutableSet.of());

    Assert.assertEquals(twoUserFriendsMap.getFriendMap(), ImmutableMap.copyOf(expectedMap));
  }

  /**
  * Tests if map is forming correctly when there are just 3 users, 2 of which are
  * friends with the third user. There are no connections other than this.
  *
  * <p>Should result in a UserFriendsMap with three keys:
  *    User C should map to a friend set with just User E
  *    User D should map to a friend set with just User E
  *    User E should map to a friend set with User C and User D.
  */
  @Test
  public void threeUsersTwoConnectionGraphTest() {
    userC = new UserNode(USER_C_ID, ImmutableSet.of(USER_E_ID));
    userD = new UserNode(USER_D_ID, ImmutableSet.of(USER_E_ID));
    userE = new UserNode(USER_E_ID, ImmutableSet.of(USER_C_ID, USER_D_ID));
    
    Set<UserNode> threeUsersTwoConnectionSet = new HashSet<>();
    threeUsersTwoConnectionSet.add(userC);
    threeUsersTwoConnectionSet.add(userD);
    threeUsersTwoConnectionSet.add(userE);
    UserFriendsMap threeUsersTwoConnectionMap = new UserFriendsMap(threeUsersTwoConnectionSet);
    
    Map<String, ImmutableSet<String>> expectedMap = new HashMap<>();
    expectedMap.put(USER_C_ID, ImmutableSet.of(USER_E_ID));
    expectedMap.put(USER_D_ID, ImmutableSet.of(USER_E_ID));
    expectedMap.put(USER_E_ID, ImmutableSet.of(USER_C_ID, USER_D_ID));

    Assert.assertEquals(threeUsersTwoConnectionMap.getFriendMap(), ImmutableMap.copyOf(expectedMap));
  }
}

