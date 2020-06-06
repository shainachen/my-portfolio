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
/*
function getMessages(value) {
  console.log("regular get message");
  fetch('/data?numberofcomments='+String(value)).then(response => response.json()).then((messages) => {
    const messagesContainer = document.getElementById('messagescontainer');
    messagesContainer.innerHTML= '';
    for (i=0; i < messages.length; i++) {
      messagesContainer.appendChild(createListElement(messages[i]));
    }
  });
}
*/

function getMessages() {
  const numComments = document.getElementById('numberofcomments').value;
  console.log(numComments);
  console.log('/data?numberofcomments='+String(numComments));
  
  fetch('/data?numberofcomments='+String(numComments)).
  then(response => response.json()).then((messages) => {
    const messageCount = document.getElementById('numberofcomments');
    const messagesContainer = document.getElementById('messagescontainer');
    messagesContainer.innerHTML= '';
    for (i=0; i < messages.length; i++) {
      messagesContainer.appendChild(createListElement(messages[i]));
    }
  });
}

function logValue(value) {
    console.log("logging value");
    console.log(value);
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}
