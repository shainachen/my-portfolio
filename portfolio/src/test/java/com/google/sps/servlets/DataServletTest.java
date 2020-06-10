package com.google.sps.servlets;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static com.google.sps.Constants.COMMENT_ENTITY_NAME;
import static com.google.sps.Constants.COMMENT_NAME_ID;
import static com.google.sps.Constants.COMMENT_TEXT_ID;
import static com.google.sps.Constants.REQUEST_NAME_PARAM;
import static com.google.sps.Constants.REQUEST_COMMENT_PARAM;
import static com.google.sps.Constants.REQUEST_NUMCOMMENTS_PARAM;

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
  private DatastoreService ds;
  private final LocalServiceTestHelper datastoreConfiguration =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    dataServlet = new DataServlet();
    dataServlet.init();
    stringWriter = new StringWriter();
    datastoreConfiguration.setUp();
    ds = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    datastoreConfiguration.tearDown();
  }

  @Test
  public void testDataServlet_doGet_returnsSingleComment() throws Exception {
    ds.put(new CommentEntity("Bob", "Nice").getEntity());
    when(request.getParameter(REQUEST_NUMCOMMENTS_PARAM)).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }

  @Test
  public void testDataServlet_doGet_returnsNoComment() throws Exception {
    when(request.getParameter(REQUEST_NUMCOMMENTS_PARAM)).thenReturn("10");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[]");
  }

  /* Default max number of comments is 10, test expected to return up to 10 comments in datastore */
  @Test
  public void testDataServlet_doGet_returnsMaxNumCommentsWithIllegalNumComments() throws Exception {
    ds.put(new CommentEntity("Bob", "Nice").getEntity());
    when(request.getParameter(REQUEST_NUMCOMMENTS_PARAM)).thenReturn("-1");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }

  @Test
  public void testDataServlet_doGet_returnsMultipleComments() throws Exception {
    ds.put(new CommentEntity("Bob", "Nice").getEntity());
    ds.put(new CommentEntity("Sally", "This is a test comment").getEntity());
    when(request.getParameter(REQUEST_NUMCOMMENTS_PARAM)).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\",\"Sally: This is a test comment\"]");
  }

  /* Default max number of comments is 10, test expected to return 10 comments */
  @Test
  public void testDataServlet_doGet_postsMaxNumCommentsWithMoreThanMaxCommentsGivenAndRequested() throws Exception {
    for (int i=0; i< 12; i++) {
      ds.put(new CommentEntity("Bob", "Nice").getEntity());
    }
    when(request.getParameter(REQUEST_NUMCOMMENTS_PARAM)).thenReturn("12");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);

    assertEquals(stringWriter.getBuffer().toString().trim(), 
    "[\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\"]");
  }

  @Test
  public void testDataServlet_doPost_postsSingleComment() throws Exception {
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn("Alice");
    when(request.getParameter(REQUEST_COMMENT_PARAM)).thenReturn("My comment");

    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, results.size());
    assertEquals("Alice", results.get(0).getProperty(COMMENT_NAME_ID));
    assertEquals("My comment", results.get(0).getProperty(COMMENT_TEXT_ID));
  }

  @Test
  public void testDataServlet_doPost_postsAnonymousSingleComment() throws Exception {
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn("");
    when(request.getParameter(REQUEST_COMMENT_PARAM)).thenReturn("My comment");

    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, results.size());
    assertEquals("", results.get(0).getProperty(COMMENT_NAME_ID));
    assertEquals("My comment", results.get(0).getProperty(COMMENT_TEXT_ID));
  }

  @Test
  public void testDataServlet_doPost_postsNothingGivenNoComment() throws Exception {
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn("Alice");

    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(0, results.size());
  }
}