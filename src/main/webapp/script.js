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
    let name = userinfo.name;
    let bio = userinfo.bio;
    let carouselContainer = document.getElementById("carousel-inner");
    let numPhotos = 5; //hard coded value for now, once blobstore works, this will be the amount of images the user has uploaded
    for (let i = 0; i < numPhotos; i++) {
      if (i === 0) {
        let slideshowElement = createSlideshowElement("images/noBlobStoreImage.jpg", "carousel-item active", name, bio);
      }
      else {
        let slideshowElement = createSlideshowElement("images/noBlobStoreImage.jpg", "carousel-item", name, bio);
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
    if (i === 0) {
      let indicator = createIndicator(i, "active");
    }
    else {
      let indicator = createIndicator(i, "");
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
      let noMatch = "NO_POTENTIAL_MATCHES";
      if (pmID.nextPotentialMatchID === noMatch) {
        noPotentialMatch();
        return;
      }
      document.getElementById("pass-btn").disabled = false;
      document.getElementById("friend-btn").disabled = false;
      displayPotentialMatchInfo(pmID.nextPotentialMatchID);
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
  // TODO: send match info to matching servlet
  getNextPotentialMatch();  
}

function passButtonPressed() {
  // TODO: send pass info to matching servlet
  getNextPotentialMatch();
}

function loadProfile() {
  let id = getCurrentUserId();
  if (id === null) {
    return;
  }
  console.log(id);
  fetch('/user-data?id=' + id).then(response => response.json()).then((userinfo) => {
    console.log(userinfo);
    name = userinfo.name;
    bio = userinfo.bio;
    document.getElementById("name").value = name;
    document.getElementById("bio").value = bio;
  });
}

function changeImgPath(blobKey, id) {
  fetch('/blob-key?imageKey='+blobKey).then((response) => {
    console.log(response);
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
