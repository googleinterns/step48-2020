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

const FacebookAPI = require("../FacebookAPI.js");

// Test the function that wraps the API call that collects Facebook user info
test("Test getUserFacebookInfoAndRedirect function", () => {
  FacebookAPI.getUserFacebookInfoAndRedirect = jest.fn((callback) => {
    // Mock API response
    const response = {
      name: "Tim",
      id: "123",
      email: "tim123@abcd.com"
    };
    return callback(response);
  });

  const mockCallback = jest.fn((response) => {
    // Create mock redirect URL
    return "/profile?name=" + response.name + "&id=" + response.id + "&email=" + response.email;
  });

  let result = FacebookAPI.getUserFacebookInfoAndRedirect(mockCallback);
  expect(result).toEqual("/profile?name=Tim&id=123&email=tim123@abcd.com");
});

// Test the function that responds to the logged in state of the user 
test("Test checkLoginState function", () => {
  FacebookAPI.checkLoginStatus = jest.fn((callback) => {
    // Mock FB login status
    const response = {
      login_status: "connected"
    };
    return callback(response);
  });

  const mockCallback = jest.fn((response) => {
    return response.login_status === "connected";
  });

  let result = FacebookAPI.checkLoginStatus(mockCallback);
  expect(result).toBe(true);
});

