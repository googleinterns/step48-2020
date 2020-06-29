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
  private static final Set<FriendRelationship> emptyFriendshipSet = new HashSet<>();
  private UserFriendsGraph friendsGraph;
  private Set<UserNode> userNodeSet;
  private Set<FriendRelationship> smallFriendshipSet;

  private static final UserNode userA = new UserNode("12345", new HashSet<String>());
  private static final UserNode userB = new UserNode("23456", new HashSet<String>());
  private UserNode userC;
  private UserNode userD;
  private UserNode userE;
  private Set<String> userCFriends;
  private Set<String> userDFriends;
  private Set<String> userEFriends;

  @Before
  public void setUp() {
    userCFriends = new HashSet<>();
    userDFriends = new HashSet<>();
    userEFriends = new HashSet<>();
    userCFriends.add("56789");
    userDFriends.add("56789");
    userEFriends.add("34567");
    userEFriends.add("45678");
    userC = new UserNode("34567", userCFriends);
    userD = new UserNode("45678", userDFriends);
    userE = new UserNode("56789", userEFriends);
  }

  @Test
  public void oneUserGraphTest() {
    Set<UserNode> oneUserSet = new HashSet<>();
    oneUserSet.add(userA);
    UserFriendsGraph oneUserGraph = new UserFriendsGraph(oneUserSet);

    Map<String, Set<String>> friendGraph = oneUserGraph.getFriendGraph();
    Set<FriendRelationship> friendshipSet = oneUserGraph.getFriendshipEdges();

    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put("12345", emptyStringSet);

    Assert.assertEquals(friendGraph, expectedGraph);
    Assert.assertEquals(friendshipSet, emptyFriendshipSet);
  }

  @Test
  public void twoUsersNoConnectionsGraphTest() {
    Set<UserNode> twoUserSet = new HashSet<>();
    twoUserSet.add(userA);
    twoUserSet.add(userB);
    UserFriendsGraph twoUserGraph = new UserFriendsGraph(twoUserSet);

    Map<String, Set<String>> friendGraph = twoUserGraph.getFriendGraph();
    Set<FriendRelationship> friendshipSet = twoUserGraph.getFriendshipEdges();

    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put("12345", emptyStringSet);
    expectedGraph.put("23456", emptyStringSet);

    Assert.assertEquals(friendGraph, expectedGraph);
    Assert.assertEquals(friendshipSet, emptyFriendshipSet);
  }

  @Test
  public void threeUsersOneConnectionGraphTest() {
    Set<UserNode> threeUsersOneConnectionSet = new HashSet<>();
    threeUsersOneConnectionSet.add(userC);
    threeUsersOneConnectionSet.add(userD);
    threeUsersOneConnectionSet.add(userE);
    UserFriendsGraph threeUsersOneConnectionGraph = new UserFriendsGraph(threeUsersOneConnectionSet);

    Map<String, Set<String>> friendGraph = threeUsersOneConnectionGraph.getFriendGraph();
    Set<FriendRelationship> friendshipSet = threeUsersOneConnectionGraph.getFriendshipEdges();
    
    Map<String, Set<String>> expectedGraph = new HashMap<>();
    expectedGraph.put("34567", userCFriends);
    expectedGraph.put("45678", userDFriends);
    expectedGraph.put("56789", userEFriends);

    Set<FriendRelationship> expectedFriendshipSet = new HashSet<>();
    expectedFriendshipSet.add(new FriendRelationship("34567", "56789"));
    expectedFriendshipSet.add(new FriendRelationship("45678", "56789"));

    Assert.assertEquals(friendGraph, expectedGraph);
    Assert.assertEquals(friendshipSet, expectedFriendshipSet);    
  }
}