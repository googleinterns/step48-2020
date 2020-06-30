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

const FacebookAPI = require("../FacebookAPI.js");

// Test the function that wraps the API call that collects Facebook user info
test("Test getUserFacebookInfoAndRedirect function", () => {
  const mockCallback = jest.fn(() => "/profile?name=Tod&id=123&email=tod@abcd.com");
  let result = FacebookAPI.getUserFacebookInfoAndRedirect(mockCallback);
  expect(result).toEqual("/profile?name=Tod&id=123&email=tod@abcd.com");
});

// Test the function that responds to the logged in state of the user 
test("Test checkLoginState function", () => {
  const mockCallback = jest.fn((loggedIn) => {
    if (loggedIn) return "connected";
    else return "not_authorized";
  });
  let result = FacebookAPI.checkLoginStatus(mockCallback);
  expect(result).toEqual('not_authorized');
});

