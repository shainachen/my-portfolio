package com.google.sps.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.sps.CommentEntity;
import com.google.sps.Comments;
import com.google.appengine.api.datastore.FetchOptions;
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
import java.util.List;

@RunWith(JUnit4.class)
public class DataServletTest {

  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;

  private DataServlet dataServlet;
  private StringWriter stringWriter;
  private final LocalServiceTestHelper datastoreConfiguration =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dataServlet = new DataServlet();
    dataServlet.init();
    stringWriter = new StringWriter();
    datastoreConfiguration.setUp();
  }

  @After
  public void tearDown() {
    datastoreConfiguration.tearDown();
  }

  @Test
  public void testDataServlet_doGet_returnsSingleComment() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    CommentEntity comment = new CommentEntity("Bob", "Nice");
    ds.put(comment.getEntity());
    when(request.getParameter("numberofcomments")).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }

  @Test
  public void testDataServlet_doGet_returnsNoComment() throws Exception {
    when(request.getParameter("numberofcomments")).thenReturn("10");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[]");
  }

  @Test
  public void testDataServlet_doGet_returnsCommentByUsingDefaultCommentNumberWithIllegalParameterSpecification() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    CommentEntity comment = new CommentEntity("Bob", "Nice");
    ds.put(comment.getEntity());
    when(request.getParameter("numberofcomments")).thenReturn("0");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }

  @Test
  public void testDataServlet_doGet_returnsMultipleComments() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    CommentEntity comment1 = new CommentEntity("Bob", "Nice");
    CommentEntity comment2 = new CommentEntity("Sally", "This is a test comment");
    ds.put(comment1.getEntity());
    ds.put(comment2.getEntity());
    when(request.getParameter("numberofcomments")).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\",\"Sally: This is a test comment\"]");
  }

  @Test
  public void testDataServlet_doPost_postsSingleComment() throws Exception {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    when(request.getParameter("name-input")).thenReturn("Alice");
    when(request.getParameter("comment-input")).thenReturn("My comment");

    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query("Comment")).asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, results.size());
    assertEquals("Alice", results.get(0).getProperty("nameText"));
    assertEquals("My comment", results.get(0).getProperty("commentText"));
  }
}