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
  fetchBlobstoreURL();
}
 
function addIdToPostRequest() {
  document.getElementById('id-input').value = getCurrentUserId();
}
 
function getCurrentUserId() {
  const params = new URLSearchParams(window.location.search);
  uid = params.get('id');
  return uid;
}

function updateProfileLinkForCurrentUser() {
  const uid = getCurrentUserId();
  const link = document.getElementById("profileLink");
  link.setAttribute('href', 'profile.html?id=' + uid);
}

function updateFeedLinkForCurrentUser() {
  const uid = getCurrentUserId();
  const link = document.getElementById("feedLink");
  link.setAttribute('href', 'feed.html?id=' + uid);
}

//This function sets the matches url to also contain the url of the 
//current user
function updateMatchesLinkForCurrentUser() {
  const uid = getCurrentUserId();
  const link = document.getElementById("matchesLink");
  link.setAttribute('href', 'matches.html?id=' + uid);
}

window.addEventListener("load", function(){
  function a(a,b){var c=/^(?:file):/,d=new XMLHttpRequest,e=0;d.onreadystatechange=function(){4==d.readyState&&(e=d.status),c.test(location.href)&&d.responseText&&(e=200),4==d.readyState&&200==e&&(a.outerHTML=d.responseText)};try{d.open("GET",b,!0),d.send()}catch(f){}}var b,c=document.getElementsByTagName("*");for(b in c)c[b].hasAttribute&&c[b].hasAttribute("data-include")&&a(c[b],c[b].getAttribute("data-include"));
});

function getMutualFriends(pmID, callback) {
  const currentUser = getCurrentUserId();
  fetch('/mutual-friends?userid1=' + currentUser + '&userid2=' + pmID).then(response => response.json()).then(callback);
}

function displayPotentialMatchInfo(pmID) {
  console.log(currentPMDisplayed);
  fetch('/user-data?id=' + pmID).then(response => response.json()).then((userinfo) => {
    const name = userinfo.name;
    const bio = userinfo.bio;
    getMutualFriends(pmID, mutualFriends => {
      const carouselContainer = document.getElementById("carousel-inner");
      let numPhotos = 0;
      for (let i = 0; i < userinfo.blobkeys.length; i++) {
        if (userinfo.blobkeys[i] !== "") {
          numPhotos++;
          const imageElement = createImageFromBlobstore(userinfo.blobkeys[i]);
          const slideshowElement = createSlideshowElement(imageElement, "carousel-item" + (i === 0 ? " active" : ""), name, bio, mutualFriends);
          carouselContainer.appendChild(slideshowElement);
        }
      }
      if (numPhotos === 0) {
        const noImageElement = createImgElement("images/no_image.png");
        const noImageSlideshowElement = createSlideshowElement(noImageElement, "carousel-item active", name, bio);
        carouselContainer.appendChild(noImageSlideshowElement);
      }
      addIndicators(numPhotos);
    });
  });
}

function deletePotentialMatchInfo() {
  document.getElementById("carousel-inner").innerHTML = '';
  document.getElementById("carousel-indicators").innerHTML = '';
}

function createSlideshowElement(image, className, name, bio, mutualFriends) {
  const slideshowImage = document.createElement("div"); 
  slideshowImage.className = className;
  slideshowImage.appendChild(image);
  const caption = document.createElement("div");
  caption.className = "carousel-caption";
  const header = document.createElement("h3");
  const pmName = document.createTextNode(name); 
  header.appendChild(pmName);
  caption.appendChild(header);
  const para = document.createElement("p");
  const pmBio = document.createTextNode(bio);
  para.appendChild(pmBio);
  caption.appendChild(para);
  const para2 = document.createElement("p");
  const mutualFriendsList = document.createTextNode("Mutual Friends: " + mutualFriends);
  para2.appendChild(mutualFriendsList);
  caption.appendChild(para2);
  slideshowImage.appendChild(caption); 
  return slideshowImage;
}

function addIndicators(numPhotos) {
  const indicators = document.getElementById('carousel-indicators');
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
  const singleIndicator = document.createElement("li");
  singleIndicator.setAttribute("data-target", "#feed");
  singleIndicator.setAttribute("data-slide-to", dataSlideNumber);
  singleIndicator.className = className;
  return singleIndicator;
}

function getNextPotentialMatch() {
  deletePotentialMatchInfo();
  const currentUser = getCurrentUserId();
  fetch('/potential-matches?userid=' + currentUser).then(response => response.json()).then((pmID) => { 
      if (pmID.nextPotentialMatchID === NO_MATCH) {
        noPotentialMatch();
        return;
      }
      document.getElementById("pass-btn").disabled = false;
      document.getElementById("friend-btn").disabled = false;
      displayPotentialMatchInfo(pmID.nextPotentialMatchID);
      currentPMDisplayed = pmID.nextPotentialMatchID;
  }); 
}

function noPotentialMatch() {
  document.getElementById("pass-btn").disabled = true;
  document.getElementById("friend-btn").disabled = true;
  const noPotentialMatchImage = createImgElement("images/nomatches.png")
  const noMatchImg = createSlideshowElement(noPotentialMatchImage, "carousel-item active", "", "");
  const carouselContainer = document.getElementById("carousel-inner");
  carouselContainer.appendChild(noMatchImg);
}

function matchButtonPressed() {
  const userid = getCurrentUserId();
  fetch('/match-decisions?userid=' + userid + "&potentialMatchID=" + 
    currentPMDisplayed + "&decision=" + FRIENDED, { method: 'POST' }).then((response) => {
      getNextPotentialMatch();
  });
}

function passButtonPressed() {
  const userid = getCurrentUserId();
  fetch('/match-decisions?userid=' + userid + "&potentialMatchID=" + currentPMDisplayed 
    + "&decision=" + PASSED, { method: 'POST' }).then((response) => {
      getNextPotentialMatch();
  });
}

function loadProfile() {
  const id = getCurrentUserId();
  if (id === null) {
    return;
  }
  fetch('/user-data?id=' + id).then(response => response.json()).then((userinfo) => {
    name = userinfo.name;
    bio = userinfo.bio;
    document.getElementById("name").value = name;
    document.getElementById("bio").value = bio;
    console.log(userinfo);

    // Load user images
    if (userinfo.blobkeys[0] !== "") {
      setImageFromBlobstore(userinfo.blobkeys[0], "profile-photo-image");
    }
    if (userinfo.blobkeys[1] !== "") {
      setImageFromBlobstore(userinfo.blobkeys[1], "photo-2-image");
    }
    if (userinfo.blobkeys[2] !== "") {
      setImageFromBlobstore(userinfo.blobkeys[2], "photo-3-image");
    }
    if (userinfo.blobkeys[3] !== "") {
      setImageFromBlobstore(userinfo.blobkeys[3], "photo-4-image");
    }
    if (userinfo.blobkeys[4] !== "") {
      setImageFromBlobstore(userinfo.blobkeys[4], "photo-5-image");
    }
  });
}

function displayMatches() {
  const userID = getCurrentUserId();
  if (userID === null) {
      return;
  }
  fetch('/matches-list?id=' + userID).then(response => response.json()).then((matches) => {
    const matchContainer = document.getElementById('matches-container');
    for (let i = 0; i < matches.length; i++) {
      const mutualFriends = getMutualFriends(matches[i]);
      matchContainer.appendChild(createCardElement(matches[i]), mutualFriends);
    }
  });
}

function createCardElement(userID, mutualFriends) {
  const cardDiv = document.createElement("div");
  fetch('/user-data?id=' + userID).then(response => response.json()).then((userinfo) => {
    console.log(userinfo);
    if (userinfo === null) {
        return;
    }
    cardDiv.className = "col card m-5 card-container";
    let profileImage = getFirstAvailableImage(userinfo.blobkeys);
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
      const mutualFriends = document.createTextNode("Mutual Friends: " + mutualFriends);
      para.appendChild(bio);
      para.appendChild(mutualFriends);
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

function getFirstAvailableImage(blobkeyList) {
  for (let i = 0; i < blobkeyList.length; i++) {
    if (blobkeyList[i] !== "") {
      return createImageFromBlobstore(userinfo.blobkeys[i]);
      }
  }
  return createImgElement("images/no_image.png");
}

function changeImgPath(blobKey, id) {
  fetch('/blob-key?imageKey='+blobKey).then((response) => {
    return response.blob();
  }).then((blobContent) => {
    const blobURL = URL.createObjectURL(blobContent);
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

// Set the action of the profile creation form so that information is sent to blobstore
function fetchBlobstoreURL() {
  fetch('/blobstore-url').then((response) => response.text()).then((imageUploadURL) => {
    document.getElementById("user-profile-form").action = imageUploadURL;
  });
}

// Tell the user data servlet if any images have been uploaded
function submitProfileForm() {
  if (document.getElementById("profile-photo").files.length != 0) {
    document.getElementById("profile-photo-uploaded").value = "true";
  }
  if (document.getElementById("photo-2").files.length != 0) {
    document.getElementById("photo-2-uploaded").value = "true";
  }
  if (document.getElementById("photo-3").files.length != 0) {
    document.getElementById("photo-3-uploaded").value = "true";
  }
  if (document.getElementById("photo-4").files.length != 0) {
    document.getElementById("photo-4-uploaded").value = "true";
  }
  if (document.getElementById("photo-5").files.length != 0) {
    document.getElementById("photo-5-uploaded").value = "true";
  }
}

// Function that sets the img src tag with the image corresponding to the provided blobkey
function setImageFromBlobstore(imageBlobKey, imageId) {
  fetch('/blob-key?imageKey=' + imageBlobKey).then((response) => response.blob()).then((blobContent) => {
    document.getElementById(imageId).src = URL.createObjectURL(blobContent);
  });
}

//Function that creates an image element that corresponds to the provided blobkey and returns it
function createImageFromBlobstore(imageBlobKey) {
  const imgElement = document.createElement('img');
  fetch('/blob-key?imageKey=' + imageBlobKey).then((response) => response.blob()).then((blobContent) => {
    imgElement.src = URL.createObjectURL(blobContent);
  });
  imgElement.setAttribute("height", "800");
  imgElement.setAttribute("width", "1100");
  return imgElement;
}
