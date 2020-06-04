package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DataServletTest {

  @Mock
  HttpServletRequest request;
  @Mock
  HttpServletResponse response;
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }
  @Test
  public void testDataServlet_doGet_returnsHardcodedMessage() throws Exception {
    StringWriter stringWriter = new StringWriter();

    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    DataServlet dataServlet = new DataServlet();
    dataServlet.doGet(request, response);
    String resultOfGet = stringWriter.getBuffer().toString().trim();
    assertEquals(resultOfGet, new String("[\"Wowzers\",\"Love those pictures\",\"Yes I am commenting on my own website\"]"));
  }
}