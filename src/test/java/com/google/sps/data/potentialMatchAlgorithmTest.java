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

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableSet;
import com.google.sps.data.friend_map.*;

/* NOTE: PM = Potential Match .*/

@RunWith(JUnit4.class)
public class potentialMatchAlgorithmTest {

  private static final String USER_A_ID = "12345";
  private static final String USER_B_ID = "23456";
  private static final String USER_C_ID = "34567";
  private static final String USER_D_ID = "45678";
  private static final String USER_E_ID = "56789";
  private static final String USER_F_ID = "67890";
  private static final String USER_G_ID = "78901";
  private static final String USER_H_ID = "89012";
  private static final String USER_I_ID = "90123";
  private static final String USER_J_ID = "01234";
  private static final String USER_K_ID = "11234";
  private static final String USER_L_ID = "22345";
  private static final String USER_M_ID = "33456";
  private static final String USER_N_ID = "44567";

  private UserNode userA;
  private UserNode userB;
  private UserNode userC;
  private UserNode userD;
  private UserNode userE;
  private UserNode userF;
  private UserNode userG;
  private UserNode userH;
  private UserNode userI;
  private UserNode userJ;
  private UserNode userK;
  private UserNode userL;
  private UserNode userM;
  private UserNode userN;

  private ImmutableSet<String> userAFriends;
  private ImmutableSet<String> userBFriends;
  private ImmutableSet<String> userCFriends;
  private ImmutableSet<String> userDFriends;
  private ImmutableSet<String> userEFriends;
  private ImmutableSet<String> userFFriends;
  private ImmutableSet<String> userGFriends;
  private ImmutableSet<String> userHFriends;
  private ImmutableSet<String> userIFriends;
  private ImmutableSet<String> userJFriends;
  private ImmutableSet<String> userKFriends;
  private ImmutableSet<String> userLFriends;
  private ImmutableSet<String> userMFriends;
  private ImmutableSet<String> userNFriends;

  private Set<UserNode> userNodesSet;
  private Map<String, ImmutableSet<String>> expectedMap;

  @Before
  public void setUp() {
    userNodesSet = new HashSet<>();
    expectedMap = new HashMap<>();

    userAFriends = ImmutableSet.of();

    userBFriends = ImmutableSet.of(USER_C_ID);
    userCFriends = ImmutableSet.of(USER_B_ID);

    userDFriends = ImmutableSet.of(USER_E_ID, USER_F_ID);
    userEFriends = ImmutableSet.of(USER_D_ID);
    userFFriends = ImmutableSet.of(USER_D_ID);

    userGFriends = ImmutableSet.of(USER_H_ID, USER_I_ID, USER_J_ID);
    userHFriends = ImmutableSet.of(USER_G_ID, USER_K_ID);
    userIFriends = ImmutableSet.of(USER_G_ID);
    userJFriends = ImmutableSet.of(USER_G_ID);
    userKFriends = ImmutableSet.of(USER_H_ID);

    userLFriends = ImmutableSet.of(USER_M_ID, USER_N_ID);
    userMFriends = ImmutableSet.of(USER_L_ID, USER_N_ID);
    userNFriends = ImmutableSet.of(USER_L_ID, USER_M_ID);

    userA = new UserNode(USER_A_ID, userAFriends);
    userB = new UserNode(USER_B_ID, userBFriends);
    userC = new UserNode(USER_C_ID, userCFriends);
    userD = new UserNode(USER_D_ID, userDFriends);
    userE = new UserNode(USER_E_ID, userEFriends);
    userF = new UserNode(USER_F_ID, userFFriends);
    userG = new UserNode(USER_G_ID, userGFriends);
    userH = new UserNode(USER_H_ID, userHFriends);
    userI = new UserNode(USER_I_ID, userIFriends);
    userJ = new UserNode(USER_J_ID, userJFriends);
    userK = new UserNode(USER_K_ID, userKFriends);
    userL = new UserNode(USER_L_ID, userLFriends);
  }

  @Test
  public void oneUserNoFriendsPMTest() {
    userNodesSet.add(userA);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(userNodesSet);

    
    expectedMap.put(USER_A_ID, ImmutableSet.of());

    Assert.assertEquals(expectedMap, potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap));
  }

  @Test
  public void threeUsersOneFriendshipPMTest() {
    userNodesSet.add(userA);
    userNodesSet.add(userB);
    userNodesSet.add(userC);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(userNodesSet);

    expectedMap.put(USER_A_ID, ImmutableSet.of());
    expectedMap.put(USER_B_ID, ImmutableSet.of());
    expectedMap.put(USER_C_ID, ImmutableSet.of());

    Assert.assertEquals(expectedMap, potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap));
  }

  @Test
  public void threeUsersTwoFriendshipPMTest() {
    userNodesSet.add(userD);
    userNodesSet.add(userE);
    userNodesSet.add(userF);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(userNodesSet);

    expectedMap.put(USER_D_ID, ImmutableSet.of());
    expectedMap.put(USER_E_ID, ImmutableSet.of(USER_F_ID));
    expectedMap.put(USER_F_ID, ImmutableSet.of(USER_E_ID));

    Assert.assertEquals(expectedMap, potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap));
  }

  @Test
  public void fiveUsersConnectedAndSeparatedTest() {
    userNodesSet.add(userA);
    userNodesSet.add(userB);
    userNodesSet.add(userC);
    userNodesSet.add(userD);
    userNodesSet.add(userE);
    userNodesSet.add(userF);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(userNodesSet);

    expectedMap.put(USER_A_ID, ImmutableSet.of());
    expectedMap.put(USER_B_ID, ImmutableSet.of());
    expectedMap.put(USER_C_ID, ImmutableSet.of());
    expectedMap.put(USER_D_ID, ImmutableSet.of());
    expectedMap.put(USER_E_ID, ImmutableSet.of(USER_F_ID));
    expectedMap.put(USER_F_ID, ImmutableSet.of(USER_E_ID));

    Assert.assertEquals(expectedMap, potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap));
  }

  @Test
  public void fiveUsersSeveralConnectionsTest() {
    userNodesSet.add(userG);
    userNodesSet.add(userH);
    userNodesSet.add(userI);
    userNodesSet.add(userJ);
    userNodesSet.add(userK);
    UserFriendsMap resultingFriendsMap = new UserFriendsMap(userNodesSet);

    expectedMap.put(USER_G_ID, ImmutableSet.of(USER_K_ID));
    expectedMap.put(USER_H_ID, ImmutableSet.of(USER_J_ID, USER_I_ID));
    expectedMap.put(USER_I_ID, ImmutableSet.of(USER_H_ID, USER_J_ID));
    expectedMap.put(USER_J_ID, ImmutableSet.of(USER_H_ID,USER_I_ID));
    expectedMap.put(USER_K_ID, ImmutableSet.of(USER_G_ID));

    Assert.assertEquals(expectedMap, potentialMatchAlgorithm.findAllPotentialMatches(resultingFriendsMap));
  }

  @Test
  public void threeUsersAllConnectedTest() {
    userNodesSet.add(userL);
    userNodesSet.add(userM);
    userNodesSet.add(userN);
  }
}

