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
import java.util.Optional;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that fetches and posts comments **/
@WebServlet("/add-comments")
public class DataServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = getNumberOfCommentsToDisplay(request);
    List<String> comments = new ArrayList();
    int numCommentsAdded = 0;
    for (Entity entity : DatastoreServiceFactory.getDatastoreService().prepare(new Query("Comment")).asIterable()) {
      comments.add(String.format("%s: %s",entity.getProperty("nameText"), entity.getProperty("commentText")));
      numCommentsAdded++;
      if (numCommentsAdded >= numComments) {
          break;
      }
    }

    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> commentText = Optional.ofNullable(request.getParameter("comment-input"));
    if (commentText.isPresent()) {
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("nameText", Optional.ofNullable(request.getParameter("name-input")).orElse("Anonymous"));
      commentEntity.setProperty("commentText", commentText.get());
      DatastoreServiceFactory.getDatastoreService().put(commentEntity);
    }
    response.sendRedirect("/index.html");        
  }

  private String convertToJsonUsingGson(List data) {
    Gson gson = new Gson();
    return gson.toJson(data);
  }
  
  private int getNumberOfCommentsToDisplay(HttpServletRequest request) {
    return Integer.parseInt(request.getParameter("numberofcomments"));
  }
}
