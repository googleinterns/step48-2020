// Copyright 2020 Google LLC
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

function getCurrentUserId() {
  const params = new URLSearchParams(window.location.search);
  uid = params.get('id');
  return uid;
}

function updateProfileLinkForCurrentUser() {
  let uid = getCurrentUserId();
  var link = document.getElementById("profileLink");
  link.setAttribute('href', 'profile.html?id=' + uid);
}

function updateFeedLinkForCurrentUser() {
  let uid = getCurrentUserId();
  var link = document.getElementById("feedLink");
  link.setAttribute('href', 'feed.html?id=' + uid);
}

function updateMatchesLinkForCurrentUser() {
  let uid = getCurrentUserId();
  var link = document.getElementById("matchesLink");
  link.setAttribute('href', 'matches.html?id=' + uid);
}

function loadAllUpdatedLinks() {
    updateProfileLinkForCurrentUser();
    updateFeedLinkForCurrentUser();
    updateMatchesLinkForCurrentUser();
}
window.addEventListener("load", function(){
   updateProfileLinkForCurrentUser();
    updateFeedLinkForCurrentUser();
    updateMatchesLinkForCurrentUser();
});

