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
 
// Javascript 'import' statements are not supported by browsers
// Instead, inject a script tag
function include(javascriptFile) { 
  let script = document.createElement('script'); 
  script.src = javascriptFile; 
  script.type = 'text/javascript'; 
  script.defer = true; 
  document.getElementsByTagName('head').item(0).appendChild(script); 
} 
include('./load-facebook.js');
 
// Response to the user's login status, redirect appropriately
function statusChangeCallback(response) {
  if (response.status === 'connected') {
    // User is logged in with Facebook
    // TODO: Redirect user to the correct page depending on whether their profile is complete
    // For now, redirect to the profile page
    // Query Facebook Graph API for user information
    FB.api('/me?fields=id,name,email,friends', function(response) {
      let userInfo = new URLSearchParams({
        name: response.name,
        id: response.id,
        email: response.email,
      });
 
      // Add friend ids to the query string
      // TODO: (After MVP) Replace this with jquery + ajax to POST JSON to the backend
      response.friends.data.forEach((friend) => userInfo.append("friends-list", friend.id));
 
      // Store user information in Datastore
      fetch('/user-data?' + userInfo.toString(), {
        method: 'POST',
      });
 
      // Redirect to the profile page, and pass the user's info in the URL
      window.location.href = "./profile.html?" + userInfo.toString();
    });
  }
}
 
// Called after the user logs in with Facebook
function checkLoginState() {
  FB.getLoginStatus(function(response) {
    statusChangeCallback(response);
  });
}


