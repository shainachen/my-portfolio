package com.google.sps;

import com.google.sps.CommentEntity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.util.List;
import java.util.ArrayList;

public class Comments {
  private List<CommentEntity> comments;
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public Comments() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  public List<CommentEntity> getComments() {
    comments = new ArrayList();
    for (Entity entity: datastore.prepare(new Query("Comment")).asIterable()) {
      comments.add(new CommentEntity(/*name*/ (String) entity.getProperty("nameText"), /*commentText*/ (String) entity.getProperty("commentText")));
    }
    return comments;
  }
}