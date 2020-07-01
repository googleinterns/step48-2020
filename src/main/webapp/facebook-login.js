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
include('./FacebookAPI.js');

// Async initialization of Facebook Javascript SDK
window.fbAsyncInit = function() {
  FB.init({
    // Connect Javascript SDK to the Friends of Friends Facebook App
    appId      : '313886196298676',
    // Parse social plugins on this webpage
    xfbml      : true,
    // Use the most recent version of the Facebook Graph API
    version    : 'v7.0'
  });

  // Returns the user login 'status'
  FB.getLoginStatus(function(response) {
    statusChangeCallback(response);
  });
};

// Async load of Facebook Javascript SDK
// Provided by Facebook to load Javascript SDK
(function(d, s, id) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return;
  js = d.createElement(s); js.id = id;
  js.src = "https://connect.facebook.net/en_US/sdk.js";
  fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));

// Response to the user's login status, redirect appropriately
function statusChangeCallback(response) {
  if (response.status === 'connected') {
    // User is logged in with Facebook
    // TODO: Redirect user to the correct page depending on whether their profile is complete
    // For now, redirect to the profile page
    // Query Facebook Graph API for user information
    FacebookAPI.getUserFacebookInfoAndRedirect(
      function(response) {
        let userInfo = new URLSearchParams({
          name: response.name,
          id: response.id,
          email: response.email,
        });

        // Store user information in Datastore
        // TODO: Add post request
        // Redirect to the profile page, and pass the user's info in the URL
      window.location.href = "./profile.html?" + userInfo.toString();
    });
  }
}

// Called after the user logs in with Facebook
function checkLoginState() {
  FacebookAPI.checkLoginStatus(function(response) {
    statusChangeCallback(response);
  });
}

