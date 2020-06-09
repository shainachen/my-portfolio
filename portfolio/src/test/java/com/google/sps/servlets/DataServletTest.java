package com.google.sps.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataServletTest {

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;

  private DataServlet dataServlet;
  private StringWriter stringWriter;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dataServlet = new DataServlet();
    dataServlet.init();
    stringWriter = new StringWriter();
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testDataServlet_doGet_returnsSingleComment() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    String nameText = "Bob";
    String commentText = "Nice";
    Entity comment = new Entity("Comment", nameText + ": " + commentText);
    comment.setProperty("nameText", nameText);
    comment.setProperty("commentText", commentText);
    ds.put(comment);
    
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), 
        new String("[\"Bob: Nice\"]"));
  }

  @Test
  public void testDataServlet_doGet_returnsMultipleComments() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    String nameText1 = "Bob";
    String commentText1 = "Nice";
    Entity comment1 = new Entity("Comment", nameText1 + ": " + commentText1);
    comment1.setProperty("nameText", nameText1);
    comment1.setProperty("commentText", commentText1);
    ds.put(comment1);
    String nameText2 = "Sally";
    String commentText2 = "This is a test comment";
    Entity comment2 = new Entity("Comment", nameText2 + ": " + commentText2);
    comment2.setProperty("nameText", nameText2);
    comment2.setProperty("commentText", commentText2);
    ds.put(comment2);
    
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), 
        new String("[\"Bob: Nice\",\"Sally: This is a test comment\"]"));
  }

  @Test
  public void testDataServlet_doPost_postsSingleComment() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter("name-input")).thenReturn("Alice");
    when(request.getParameter("comment-input")).thenReturn("My comment");

    dataServlet.doPost(request, response);
    
    assertEquals(1, ds.prepare(new Query("Comment")).countEntities(withLimit(10)));
  }
}
