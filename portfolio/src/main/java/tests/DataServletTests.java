import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import java.io.*;
import javax.servlet.http.*;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

@RunWith(JUnit4.class)
public class DataServletTests extends Mockito {
  
  @Test
  public void testServlet() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);       
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter();

    when(response.getWriter()).thenReturn(printWriter);
    DataServlet dataServlet = new DataServlet();
    dataServlet.doGet(request, response);
    String resultOfGet = stringWriter.getBufffer().toString().trim();
    assertEquals(resultOfGet, new String('["Wowzers","Love those pictures","Yes I am commenting on my own website"]'))
  }
}