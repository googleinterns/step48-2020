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
  const script = document.createElement('script'); 
  script.src = javascriptFile; 
  script.type = 'text/javascript'; 
  script.defer = true; 
  document.getElementsByTagName('head').item(0).appendChild(script); 
} 
include('./load-facebook.js');
 
// Redirect user if they aren't currently logged in
function statusChangeCallback(response) {
  if (response.status !== 'connected') {
    window.location.href = "./";
  }
}
 
// Log the user out of facebook, and redirect to home
function logoutFacebook() {
  FB.logout(function(response) {
    window.location.href = "./";
  });
}

