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

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.sps.data.friend_map.*;

/**
* Class that contains methods that find the potential matches for users.
*
* NOTE: Potential matches are users with who a particular user shares at least one friend with.
*/
public class potentialMatchAlgorithm {

  /**
  * Finds all of the potential matches for each user
  *
  * @param friendsMap object that contains the map of all direct friendships between all users
  * @return The map of each userID to a set of the userIDs of all their potential matches
  */
  public static ImmutableMap<String, ImmutableSet<String>> findAllPotentialMatches(UserFriendsMap friendsMap) {
    ImmutableMap.Builder<String, ImmutableSet<String>> builder = ImmutableMap.builder();

    for(String userID: friendsMap.getUserIDs()) {
      ImmutableSet<String> userPotentialMatches = findPotentialMatchesForUser(userID, friendsMap);
      builder.put(userID, userPotentialMatches);
    }
    ImmutableMap<String, ImmutableSet<String>> allPotentialMatches = builder.build();

    return allPotentialMatches;
  }

  /**
  * Finds the set of potential matches for a single user.
  *
  * Goes through the friend set of a user's friend to compile set of 
  * these potential matches.
  *
  * @param userID The userID of the user who's potential matches are being found
  * @param friendMap The map of the direct friendships between all users
  * @return The set of userIDs of the potential matches that are found
  */
  public static ImmutableSet<String> findPotentialMatchesForUser(String userID, UserFriendsMap friendsMap) {
    Set<String> potentialMatchesIDs = new HashSet<>();
    ImmutableSet<String> userFriendIDs = friendsMap.getUserFriendIDs(userID);

    for (String friendID: userFriendIDs) {
      potentialMatchesIDs.addAll(friendsMap.getUserFriendIDs(friendID));
    }

    potentialMatchesIDs.remove(userID);
    potentialMatchesIDs = Sets.difference(potentialMatchesIDs, userFriendIDs);

    return ImmutableSet.copyOf(potentialMatchesIDs);
  }
}

