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
 
package com.google.sps;
 
import static com.google.sps.Constants.COMMENT_ENTITY_NAME;
import static com.google.sps.Constants.COMMENT_NAME_ID;
import static com.google.sps.Constants.COMMENT_TEXT_ID;
import static com.google.sps.Constants.DEFAULT_NUM_COMMENTS;
import static com.google.sps.Constants.INDEX_URL;
import static com.google.sps.Constants.REQUEST_COMMENT_PARAM;
import static com.google.sps.Constants.REQUEST_NAME_PARAM;
import static com.google.sps.Constants.REQUEST_NUM_COMMENTS_PARAM;
 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.sps.CommentEntity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/** Servlet that fetches and posts comments **/
@WebServlet("/add-comments")
public class DataServlet extends HttpServlet {
  private DatastoreService datastore;
  @Override
  public void init() throws ServletException {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    int numComments = getNumberOfCommentsToDisplay(request);
    List<String> comments = new ArrayList();
    int numCommentsAdded = 0;   
    StreamSupport.stream(datastore.prepare(new Query(COMMENT_ENTITY_NAME)).asIterable().spliterator(), false)
    .limit(numComments)
    .forEach(entity -> {comments.add(String.format("%s: %s",entity.getProperty(COMMENT_NAME_ID), entity.getProperty(COMMENT_TEXT_ID)));});
 
    response.setContentType("application/json");
    response.getWriter().println(convertToJsonUsingGson(comments));
  }
 
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Optional<String> commentText = Optional.ofNullable(request.getParameter(REQUEST_COMMENT_PARAM));
    if (commentText.isPresent()) {
      String name = request.getParameter(REQUEST_NAME_PARAM);
      CommentEntity comment;
      if (name.equals("")) {
        comment = CommentEntity.create(commentText.get());
      } else {
        comment = CommentEntity.create(name, commentText.get());
      }
      datastore.put(comment.toEntity());
    }
    response.sendRedirect(INDEX_URL);        
  }
 
  private String convertToJsonUsingGson(List data) {
    Gson gson = new Gson();
    return gson.toJson(data);
  }
  
  private int getNumberOfCommentsToDisplay(HttpServletRequest request) {
    int numComments = Integer.parseInt(request.getParameter(REQUEST_NUM_COMMENTS_PARAM));
    if (numComments < 0) {
      numComments = DEFAULT_NUM_COMMENTS;
    }
    return numComments;
  }
}
