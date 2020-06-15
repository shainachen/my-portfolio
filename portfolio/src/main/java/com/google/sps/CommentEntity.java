package com.google.sps;

import static com.google.sps.Constants.COMMENT_ENTITY_NAME;
import static com.google.sps.Constants.COMMENT_NAME_ID;
import static com.google.sps.Constants.COMMENT_TEXT_ID;

import com.google.appengine.api.datastore.Entity;
import com.google.auto.value.AutoValue;

@AutoValue
/* A comment made by a user */
abstract class CommentEntity {
  static CommentEntity create(String name, String commentText) {
    return new AutoValue_CommentEntity(name, commentText);
  }

  abstract String name();
  abstract String commentText();

  public Entity toEntity() {
    Entity entity = new Entity(COMMENT_ENTITY_NAME);
    entity.setProperty(COMMENT_NAME_ID, name());
    entity.setProperty(COMMENT_TEXT_ID, commentText());
    return entity;
  }
}