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

// Handles calls to the Facebook API, provides wrapper methods with callbacks
class FacebookAPI {
  static getUserFacebookInfoAndRedirect(callback) {
    return callback();
  }

  static checkLoginStatus(callback) {
    return callback();
  }
}

// Export module for testing with Jest framework
module.exports = FacebookAPI;
