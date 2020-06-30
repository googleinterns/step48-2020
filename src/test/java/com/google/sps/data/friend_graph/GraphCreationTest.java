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

/** */
@RunWith(JUnit4.class)
public final class GraphCreationTest {
  private static final Set<String> emptyStringSet = new HashSet<>();
  private static final Set<UserNode> emptyUserNodeSet = new HashSet<>();

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
  private Set<String> userAFriends;
  private Set<String> userBFriends;
  private Set<String> userCFriends;
  private Set<String> userDFriends;
  private Set<String> userEFriends;

  @Before
  public void setUp() {
    userAFriends = new HashSet<>();
    userBFriends = new HashSet<>();
    userCFriends = new HashSet<>();
    userDFriends = new HashSet<>();
    userEFriends = new HashSet<>();
    userA = new UserNode(USER_A_ID, userAFriends);
    userB = new UserNode(USER_B_ID, userBFriends);
    userC = new UserNode(USER_C_ID, userCFriends);
    userD = new UserNode(USER_D_ID, userDFriends);
    userE = new UserNode(USER_E_ID, userEFriends);
  }

  /**
  * Tests if graph is forming correctly when there is only one user with 0 friends.
  *
  * Should result in a UserFriendsGraph with a map that has a single key that maps
  * to an empty friend set.
  */
  @Test
  public void oneUserGraphTest() {
    Set<UserNode> oneUserSet = new HashSet<>();
    oneUserSet.add(userA);
    UserFriendsGraph oneUserGraph = new UserFriendsGraph(oneUserSet);

    Map<String, Set<String>> friendGraph = oneUserGraph.getFriendGraph();

    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put(USER_A_ID, emptyStringSet);

    Assert.assertEquals(friendGraph, expectedGraph);
  }

  /**
  * Tests if graph is forming correctly when there are two users with 0 friends.
  *
  * Should result in a UserFriendsGraph with a map that has two keys each of which
  * map to an empty friend set.
  */
  @Test
  public void twoUsersNoConnectionsGraphTest() {
    Set<UserNode> twoUserSet = new HashSet<>();
    twoUserSet.add(userA);
    twoUserSet.add(userB);
    UserFriendsGraph twoUserGraph = new UserFriendsGraph(twoUserSet);

    Map<String, Set<String>> friendGraph = twoUserGraph.getFriendGraph();

    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put(USER_A_ID, emptyStringSet);
    expectedGraph.put(USER_B_ID, emptyStringSet);

    Assert.assertEquals(friendGraph, expectedGraph);
  }

  /**
  * Tests if graph is forming correctly when there are just 3 users, 2 of which are
  * friends with the third user. There are no connections other than this.
  *
  * Should result in a UserFriendsGraph with three keys:
  *    User C should map to a friend set with just User E
  *    User D should map to a friend set with just User E
  *    User E should map to a friend set with User C and User D.
  */
  @Test
  public void threeUsersOneConnectionGraphTest() {
    userC.addToUserFriends(USER_E_ID);
    userD.addToUserFriends(USER_E_ID);
    userE.addToUserFriends(USER_C_ID);
    userE.addToUserFriends(USER_D_ID);
    Set<UserNode> threeUsersOneConnectionSet = new HashSet<>();
    threeUsersOneConnectionSet.add(userC);
    threeUsersOneConnectionSet.add(userD);
    threeUsersOneConnectionSet.add(userE);
    UserFriendsGraph threeUsersOneConnectionGraph = new UserFriendsGraph(threeUsersOneConnectionSet);

    Map<String, Set<String>> friendGraph = threeUsersOneConnectionGraph.getFriendGraph();
    
    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put(USER_C_ID, userCFriends);
    expectedGraph.put(USER_D_ID, userDFriends);
    expectedGraph.put(USER_E_ID, userEFriends);

    Assert.assertEquals(friendGraph, expectedGraph);
  }
}

