package com.google.sps;

import static com.google.sps.Constants.COMMENT_ENTITY_NAME;
import static com.google.sps.Constants.COMMENT_NAME_ID;
import static com.google.sps.Constants.COMMENT_TEXT_ID;


import com.google.appengine.api.datastore.Entity;

/* A comment made by a user */
public class CommentEntity {
  private Entity entity;

  public CommentEntity(String name, String commentText) {
    this.entity = new Entity(COMMENT_ENTITY_NAME);
    this.entity.setProperty(COMMENT_NAME_ID, name);
    this.entity.setProperty(COMMENT_TEXT_ID, commentText);
  }

  public String getName() {
    return (String) this.entity.getProperty(COMMENT_NAME_ID);
  }

  public String getCommentText() {
    return (String) this.entity.getProperty(COMMENT_TEXT_ID);
  }

  public Entity getEntity() {
    return entity;
  }
}