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

const NO_MATCH =  "NO_POTENTIAL_MATCHES"; // must be kept in sync with the PotentialMatches.java servlet
const FRIENDED = "FRIENDED";
const PASSED = "PASSED";
let currentPMDisplayed;
 
// Function that is called onLoad of the body tag
function initializeProfilePage() {
  addIdToPostRequest();
  loadProfile();
}
 
function addIdToPostRequest() {
  document.getElementById('id-input').value = getCurrentUser();
}
 
function getCurrentUser() {
  const params = new URLSearchParams(window.location.search);
  uid = params.get('id');
  return uid;
}

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

window.addEventListener("load", function(){
  function a(a,b){var c=/^(?:file):/,d=new XMLHttpRequest,e=0;d.onreadystatechange=function(){4==d.readyState&&(e=d.status),c.test(location.href)&&d.responseText&&(e=200),4==d.readyState&&200==e&&(a.outerHTML=d.responseText)};try{d.open("GET",b,!0),d.send()}catch(f){}}var b,c=document.getElementsByTagName("*");for(b in c)c[b].hasAttribute&&c[b].hasAttribute("data-include")&&a(c[b],c[b].getAttribute("data-include"));
});

function displayPotentialMatchInfo(pmID) {
  fetch('/user-data?id=' + pmID).then(response => response.json()).then((userinfo) => {
    const name = userinfo.name;
    const bio = userinfo.bio;
    let carouselContainer = document.getElementById("carousel-inner");
    const numPhotos = 5; //hard coded value for now, once blobstore works, this will be the amount of images the user has uploaded
    for (let i = 0; i < numPhotos; i++) {
      let slideshowElement;
      if (i === 0) {
        slideshowElement = createSlideshowElement("images/noBlobStoreImage.jpg", "carousel-item active", name, bio);
      }
      else {
        slideshowElement = createSlideshowElement("images/noBlobStoreImage.jpg", "carousel-item", name, bio);
      }
      carouselContainer.appendChild(slideshowElement);
    }
    addIndicators(numPhotos);
  });
}

function deletePotentialMatchInfo() {
  document.getElementById("carousel-inner").innerHTML = '';
  document.getElementById("carousel-indicators").innerHTML = '';
}

function createSlideshowElement(blobkey, className, name, bio) {
  var slideshowImage = document.createElement("div"); 
  slideshowImage.className = className;
  slideshowImage.appendChild(createImgElement(blobkey));
  var caption = document.createElement("div");
  caption.className = "carousel-caption";
  var header = document.createElement("h3");
  var name = document.createTextNode(name); 
  header.appendChild(name);
  caption.appendChild(header);
  var para = document.createElement("p");
  var bio = document.createTextNode(bio);
  para.appendChild(bio);
  caption.appendChild(para);
  slideshowImage.appendChild(caption); 
  return slideshowImage;
}

function addIndicators(numPhotos) {
  let indicators = document.getElementById('carousel-indicators');
  for (let i = 0; i < numPhotos; i++) {
    let indicator;
    if (i === 0) {
      indicator = createIndicator(i, "active");
    }
    else {
      indicator = createIndicator(i, "");
    }
    indicators.appendChild(indicator);
  }
}

function createIndicator(dataSlideNumber, className) {
  var singleIndicator = document.createElement("li");
  singleIndicator.setAttribute("data-target", "#feed");
  singleIndicator.setAttribute("data-slide-to", dataSlideNumber);
  singleIndicator.className = className;
  return singleIndicator;
}

function getNextPotentialMatch() {
  deletePotentialMatchInfo();
  let currentUser = getCurrentUserId();
  fetch('/potential-matches?userid=' + currentUser).then(response => response.json()).then((pmID) => { 
      if (pmID.nextPotentialMatchID === NO_MATCH) {
        noPotentialMatch();
        return;
      }
      document.getElementById("pass-btn").disabled = false;
      document.getElementById("friend-btn").disabled = false;
      displayPotentialMatchInfo(pmID.nextPotentialMatchID);
      currentPMdisplayed = pmID.nextPotentialMatchID;
  }); 
}

function noPotentialMatch() {
  document.getElementById("pass-btn").disabled = true;
  document.getElementById("friend-btn").disabled = true;
  let noMatchImg = createSlideshowElement("images/nomatches.png", "carousel-item active", "", "");
  let carouselContainer = document.getElementById("carousel-inner");
  carouselContainer.appendChild(noMatchImg);
}

function matchButtonPressed() {
  const decision = {
      userid: getCurrentUserId(),
      potentialMatchID: currentPMDisplayed,
      decision: FRIENDED
  }
  fetch('/match-decisions', { method: 'POST', body: JSON.stringify(decision) }).then((response) => {
      getNextPotentialMatch();
  });
}

function passButtonPressed() {
  const decision = {
      userid: getCurrentUserId(),
      potentialMatchID: currentPMDisplayed,
      decision: PASSED
  }
  fetch('/match-decisions', { method: 'POST', body: JSON.stringify(decision) }).then((response) => {
      getNextPotentialMatch();
  });
}

function loadProfile() {
  let userID = getCurrentUserId();
  if (userID === null) {
    return;
  }
  fetch('/user-data?id=' + userID).then(response => response.json()).then((userinfo) => {
    name = userinfo.name;
    bio = userinfo.bio;
    document.getElementById("name").value = name;
    document.getElementById("bio").value = bio;
  });
}

function displayMatches() {
  const userID = getCurrentUserId();
  if (userID === null) {
      return;
  }
  fetch('/matches?id=' + userID).then(response => response.json()).then((matches) => {
    const matchContainer = document.getElementById('matches-container');
    for (let i = 0; i < matches.length; i++) {
      matchContainer.appendChild(createCardElement(matches[i]));
    }
  });
}

function createCardElement(userID) {
  const cardDiv = document.createElement("div");
  fetch('/user-data?id=' + userID).then(response => response.json()).then((userinfo) => {
    if (userinfo === null) {
        return;
    }
    cardDiv.className = "col card m-5 card-container";
    const profileImage = createImgElement("images/noBlobStoreImage2.jpg");
    profileImage.className = "card-img-top";
    profileImage.setAttribute("height", "300");
    profileImage.setAttribute("width", "100");
    const cardBody = document.createElement("div");
    cardBody.className = "card-body";
    cardDiv.appendChild(profileImage);
    const header = document.createElement("h4");
    const name = document.createTextNode(userinfo.name);
    header.appendChild(name);
    cardBody.appendChild(header);
    header.className = "card-title";
    if (userinfo.bio) {
      const para = document.createElement("p");
      const bio = document.createTextNode(userinfo.bio);
      para.appendChild(bio);
      para.className = "card-text";
      cardBody.appendChild(para);
    }
    if (userinfo.profileLink) {
      const link = document.createElement("a");
      link.setAttribute('href', userinfo.profileLink);
      const text = document.createTextNode("See Profile");
      link.appendChild(text);
      link.className = "btn see-profile-btn";
      cardBody.appendChild(link);
    }
    cardDiv.appendChild(cardBody);
  });
  return cardDiv;
}

function changeImgPath(blobKey, id) {
  fetch('/blob-key?imageKey='+blobKey).then((response) => {
    return response.blob();
  }).then((blobContent) => {
    var blobURL = URL.createObjectURL(blobContent);
    document.getElementById(id).src = blobURL;
  });  
}

function createImgElement(imageURL) {
  const imgElement = document.createElement('img');
  imgElement.src = imageURL;
  imgElement.setAttribute("height", "800");
  imgElement.setAttribute("width", "1100");
  return imgElement;
}
