package com.google.sps;
 
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static com.google.sps.Constants.COMMENT_ENTITY_NAME;
import static com.google.sps.Constants.COMMENT_NAME_ID;
import static com.google.sps.Constants.COMMENT_TEXT_ID;
import static com.google.sps.Constants.REQUEST_COMMENT_PARAM;
import static com.google.sps.Constants.REQUEST_NAME_PARAM;
import static com.google.sps.Constants.REQUEST_NUM_COMMENTS_PARAM;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.CommentEntity;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
 
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
    ds.put(CommentEntity.create("Bob", "Nice").toEntity());
    when(request.getParameter(REQUEST_NUM_COMMENTS_PARAM)).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);
 
    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }
 
  @Test
  public void testDataServlet_doGet_returnsNoCommentsWithNoneInDatastore() throws Exception {
    when(request.getParameter(REQUEST_NUM_COMMENTS_PARAM)).thenReturn("10");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);
 
    assertEquals(stringWriter.getBuffer().toString().trim(), "[]");
  }
 
  /* Default max number of comments is 10, test expected to return up to 10 comments in datastore */
  @Test
  public void testDataServlet_doGet_returnsMaxNumCommentsWithIllegalNumComments() throws Exception {
    ds.put(CommentEntity.create("Bob", "Nice").toEntity());
    when(request.getParameter(REQUEST_NUM_COMMENTS_PARAM)).thenReturn("-1");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);
 
    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\"]");
  }
 
  @Test
  public void testDataServlet_doGet_returnsMultipleComments() throws Exception {
    ds.put(CommentEntity.create("Bob", "Nice").toEntity());
    ds.put(CommentEntity.create("Sally", "This is a test comment").toEntity());
    when(request.getParameter(REQUEST_NUM_COMMENTS_PARAM)).thenReturn("5");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);
 
    assertEquals(stringWriter.getBuffer().toString().trim(), "[\"Bob: Nice\",\"Sally: This is a test comment\"]");
  }
 
  /* Default max number of comments is 10, test expected to return 10 comments */
  @Test
  public void testDataServlet_doGet_postsMaxNumCommentsWithMoreThanMaxCommentsGivenAndRequested() throws Exception {
    for (int i=0; i< 12; i++) {
      ds.put(CommentEntity.create("Bob", "Nice").toEntity());
    }
    when(request.getParameter(REQUEST_NUM_COMMENTS_PARAM)).thenReturn("12");
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    
    dataServlet.doGet(request, response);
 
    assertEquals(stringWriter.getBuffer().toString().trim(), 
    "[\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\",\"Bob: Nice\"]");
  }
 
  @Test
  public void testDataServlet_doPost_postsSingleComment() throws Exception {
    String commentName = "Alice";
    String commentText = "My comment";
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn(commentName);
    when(request.getParameter(REQUEST_COMMENT_PARAM)).thenReturn(commentText);
 
    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, results.size());
    assertEquals(commentName, results.get(0).getProperty(COMMENT_NAME_ID));
    assertEquals(commentText, results.get(0).getProperty(COMMENT_TEXT_ID));
  }
 
  @Test
  public void testDataServlet_doPost_postsAnonymousSingleComment() throws Exception {
    String commentName = "";
    String commentText = "My comment";
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn(commentName);
    when(request.getParameter(REQUEST_COMMENT_PARAM)).thenReturn(commentText);
 
    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(1, results.size());
    assertEquals(commentName, results.get(0).getProperty(COMMENT_NAME_ID));
    assertEquals(commentText, results.get(0).getProperty(COMMENT_TEXT_ID));
  }
 
  @Test
  public void testDataServlet_doPost_postsNothingGivenNoComment() throws Exception {
    String commentName = "";
    when(request.getParameter(REQUEST_NAME_PARAM)).thenReturn(commentName);
 
    dataServlet.doPost(request, response);
    
    List<Entity> results = ds.prepare(new Query(COMMENT_ENTITY_NAME)).asList(FetchOptions.Builder.withDefaults());
    assertEquals(0, results.size());
  }
}
