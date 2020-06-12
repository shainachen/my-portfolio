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

google.charts.load('current', {'packages':['corechart', 'orgchart']});
google.charts.setOnLoadCallback(drawPieChart);
google.charts.setOnLoadCallback(drawOrgChart);

/** Creates a chart and adds it to the page. */
function drawPieChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Cities');
  data.addColumn('number', 'Years');
        data.addRows([
          ['Berkeley', 2],
          ['Shanghai', 1],
          ['Sunnyvale', 17]
        ]);

  const options = {
    'title': 'Years spent at cities',
    'width':500,
    'height':400
  };

  const chart = new google.visualization.PieChart(
      document.getElementById('piechart'));
  chart.draw(data, options);
}

 function drawOrgChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Name');
        data.addColumn('string', 'Parent');
        data.addColumn('string', 'Tooltip');

        data.addRows([
          [{'v':'Mother', 'f':'Mother<div style="color:blue; font-style:italic">CEO</div>'},
           '', 'The CEO'],
          [{'v':'Clique', 'f':'Clique<div style="color:blue; font-style:italic">Terrific Trio</div>'},
           'Mother', ''],
          ['Cindy', 'Clique', 'Friend'],
          ['Shaina', 'Clique', 'Creator of this site'],
          ['Shivani', 'Clique', 'Friend']
        ]);

        // Create the chart.
        var chart = new google.visualization.OrgChart(document.getElementById('orgchart'));
        // Draw the chart, setting the allowHtml option to true for the tooltips.
        chart.draw(data, {'allowHtml':true});
      }

/** Creates a map and adds it to the page. */
function createMap() {
  const berkeley = {
    lat: 37.871853,
    lng: -122.258423
  };
  const map = new google.maps.Map(
    document.getElementById('map'), {
      center: berkeley,
      zoom: 15
    });

  addLandmark(map, /* lat= */ 37.8703, /* lng= */ -122.2595,
    /* title= */
    'Sather Gate',
    /* description= */
    "<img src='images/mapImages/SatherGate.JPG'>\
    <div>Sather Gate: Berkeley's green gate to campus</div>");

  addLandmark(map, /* lat= */ 37.8746, /* lng= */ -122.2496,
    /* title= */
    'Best views of Berkeley',
    /* description= */
    "<img src='images/mapImages/BigC.JPG'>\
    <div>This 10 minute hike gives you the best views of Berkeley's 'C'\
     and the SF bay... plus, a swing!</div>");

  addLandmark(map, /* lat= */ 37.8719, /* lng= */ -122.2577,
    /* title= */
    'Campanile',
    /* description= */
    "<img src='images/mapImages/Campanile.JPG'>\
    <div>Iconic bell tower of Berkeley. The rings signal the hours...\
    so you'll never miss a final</div>");

  addLandmark(map, /* lat= */ 37.8786, /* lng= */ -122.2686,
    /* title= */
    "Greatest potato puffs you'll ever eat",
    /* description= */
    "<img src='images/mapImages/PotatoPuff.JPG'>\
    <div>Find the best potato puffs and a menu that changes with the seasons here.</div>");
}

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title, description) {
  const marker = new google.maps.Marker({
    position: {
      lat: lat,
      lng: lng
    },
    map: map,
    title: title
  });

  const infoWindow = new google.maps.InfoWindow({
    content: description
  });
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts = ['I have been an extra in a movie before!', 'I was once allergic to peanut butter but got over it.',
    'I can understand 3 languages but only speak 2', 'My email is my first name and my neighbor\'s dog\'s name'
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
 * Gets comments from server.
 */
function getComments() {
  fetch('/add-comments?numberofcomments=' + String(document.getElementById('numberofcomments').value)).
  then(response => response.json()).then((comments) => {
    const commentsContainer = document.getElementById('commentscontainer');
    commentsContainer.innerHTML = '';
    for (i = 0; i < comments.length; i++) {
      commentsContainer.appendChild(createListElement(comments[i]));
    }
  });
}

/**
 * Creates single list element for HTML list.
 */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}