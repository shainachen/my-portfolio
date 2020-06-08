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

package com.google.sps.servlets;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**Servlet for fetching and posting comments**/
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = getNumberOfCommentsToDisplay(request);

    if (numComments == -1) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter an integer between 1 and 10.");
      return;
    }
    
    List<String> messages = new ArrayList();
    int numMessagesAdded = 0;
    for (Entity entity : DatastoreServiceFactory.getDatastoreService().prepare(new Query("Comment")).asIterable()) {
      String name = (String) entity.getProperty("nameText");
      String comment = (String) entity.getProperty("commentText");
      messages.add(name + ": " + comment);
      numMessagesAdded++;
      if (numMessagesAdded >= numComments) {
          break;
      }
    }

    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(messages));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String nameText = getParameter(request, "name-input", "No name");
    String commentText = getParameter(request, "comment-input", "No comment");
    Entity commentEntity = new Entity("Comment", nameText+commentText);
 
    commentEntity.setProperty("nameText", nameText);
    commentEntity.setProperty("commentText", commentText);
    DatastoreServiceFactory.getDatastoreService().put(commentEntity);
 
    response.sendRedirect("/index.html");
  }

  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  private String convertToJsonUsingGson(List messages) {
    Gson gson = new Gson();
    return gson.toJson(messages);
  }

  private int getNumberOfCommentsToDisplay(HttpServletRequest request) {
    String numCommentsInput = request.getParameter("numberofcomments");
    int numComments;

    try {
      numComments = Integer.parseInt(numCommentsInput);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsInput);
      return -1;
    }
    return numComments;
  }
}