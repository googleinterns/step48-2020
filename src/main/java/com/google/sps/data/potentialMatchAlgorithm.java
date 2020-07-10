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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.sps.data.friend_map.UserFriendsMap;
import com.google.sps.data.friend_map.UserNode;

/**
* Class that contains methods that find the potential matches for users.
*
* <p>Potential matches are users with who a particular user shares at least one friend with.
*/
public class potentialMatchAlgorithm {

  /**
  * Finds all of the potential matches for each user
  *
  * @param friendsMap object that contains the map of all direct friendships between all users
  * @return The map of each user ID to a set of the user IDs of all their potential matches
  */
  public static ImmutableMap<String, ImmutableSet<String>> findAllPotentialMatches(UserFriendsMap friendsMap) {
    Set<String> allUserIDs = friendsMap.getUserIDs();

    ImmutableMap<String, ImmutableSet<String>> allPotentialMatches = allUserIDs
      .stream()
      .collect(ImmutableMap.toImmutableMap(id -> id, id -> findPotentialMatchesForUser(id, friendsMap)));

    return allPotentialMatches;
  }

  /**
  * Finds the set of potential matches for a single user.
  *
  * @param userID The user ID of the user who's potential matches are being found
  * @param friendMap The map of the direct friendships between all users
  * @return The set of user IDs of the potential matches that are found
  */
  public static ImmutableSet<String> findPotentialMatchesForUser(String userID, UserFriendsMap friendsMap) {
    ImmutableSet<String> userFriendIDs = friendsMap.getUserFriendIDs(userID);

    ImmutableSet<String> potentialMatchesIDs = userFriendIDs
      .stream()
      .flatMap(friendID -> friendsMap.getUserFriendIDs(friendID).stream())
      .filter(potentialMatchID -> !potentialMatchID.equals(userID) && !userFriendIDs.contains(potentialMatchID))
      .collect(ImmutableSet.toImmutableSet());

    return potentialMatchesIDs;
  }
}

