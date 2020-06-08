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

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I have been an extra in a movie before!', 'I was once allergic to peanut butter but got over it.', 
      'I can understand 3 languages but only speak 2', 'My email is my first name and my neighbor\'s dog\'s name'];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Gets comments from datastore
 */
function getComments() {  
  fetch('/data?numberofcomments='+String(document.getElementById('numberofcomments').value)).
  then(response => response.json()).then((comments) => {
    const commentsContainer = document.getElementById('commentscontainer');
    commentsContainer.innerHTML= '';
    for (i=0; i < comments.length; i++) {
      commentsContainer.appendChild(createListElement(comments[i]));
    }
  });
}

/**
 * Creates single list element for HTML list
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
