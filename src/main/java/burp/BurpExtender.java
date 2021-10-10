package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;

public class BurpExtender implements IBurpExtender, IContextMenuFactory {

  private IContextMenuInvocation menuInvocation;
  private IBurpExtenderCallbacks burpCallbacks;
  private IExtensionHelpers burpHelpers;
  private PrintWriter stdout;
  private PrintWriter stderr;

  private static String EXTENSION_NAME = "LazyCSRF";
  private static String JSON_CSRF_MENU_NAME = "Generate JSON CSRF PoC with XHR";
  private static String FORM_CSRF_MENU_NAME = "Generate POST CSRF PoC with Form";

  @Override
  public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
    this.burpCallbacks = callbacks;
    this.burpCallbacks.setExtensionName(EXTENSION_NAME);
    this.burpCallbacks.registerContextMenuFactory(this);
    this.burpHelpers = this.burpCallbacks.getHelpers();

    stdout = new PrintWriter(this.burpCallbacks.getStdout(), true);
    stderr = new PrintWriter(this.burpCallbacks.getStderr(), true);
    stdout.println("INFO: Hello from LazyCSRF");
  }

  @Override
  public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
    List<JMenuItem> menuList = new ArrayList<>();
    menuInvocation = invocation;
    if (menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY ||
        menuInvocation.getInvocationContext()
            == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST ||
        menuInvocation.getInvocationContext()
            == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_RESPONSE ||
        menuInvocation.getInvocationContext()
            == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST ||
        menuInvocation.getInvocationContext()
            == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_RESPONSE
    ) {
      JMenuItem GenerateJsonPocButton = new JMenuItem(JSON_CSRF_MENU_NAME);
      GenerateJsonPocButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          if (arg0.getActionCommand().equals(JSON_CSRF_MENU_NAME)) {
            IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
            for (IHttpRequestResponse req : selectedMessages) {
              IRequestInfo reqInfo = burpHelpers.analyzeRequest(req);
              CSRFPoCWindow view = new CSRFPoCWindow(EXTENSION_NAME);
              view.setVisible();
              String pocText = null;
              try {
                pocText = GenerateJSONPoC(req, reqInfo);
              } catch (UnsupportedEncodingException e) {
                stderr.println("ERROR: Unsupported Encoding");
              }
              view.setRequestLabel(reqInfo.getUrl().toString());
              view.setCSRFPoCHTML(pocText);
              String reqFullText = new StringBuilder()
                  .append(parseHeaderText(reqInfo.getHeaders()))
                  .append(parseBodyText(req.getRequest()))
                  .toString();
              view.setRequest(reqFullText);
            }
          }
        }
      });
      menuList.add(GenerateJsonPocButton);

      JMenuItem GenerateFormPocButton = new JMenuItem(FORM_CSRF_MENU_NAME);
      GenerateFormPocButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          if (arg0.getActionCommand().equals(FORM_CSRF_MENU_NAME)) {
            IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
            for (IHttpRequestResponse req : selectedMessages) {
              IRequestInfo reqInfo = burpHelpers.analyzeRequest(req);
              CSRFPoCWindow view = new CSRFPoCWindow(EXTENSION_NAME);
              view.setVisible();
              String pocText = GenerateFormPoC(req, reqInfo);
              view.setRequestLabel(reqInfo.getUrl().toString());
              view.setCSRFPoCHTML(pocText);
              String reqFullText = new StringBuilder()
                  .append(parseHeaderText(reqInfo.getHeaders()))
                  .append(parseBodyText(req.getRequest()))
                  .toString();
              view.setRequest(reqFullText);
            }
          }
        }
      });
      menuList.add(GenerateFormPocButton);
    }
    return menuList;
  }

  private String GenerateJSONPoC(IHttpRequestResponse req, IRequestInfo reqInfo)
      throws UnsupportedEncodingException {
    String method = reqInfo.getMethod();
    String body = new String(req.getRequest(), StandardCharsets.UTF_8)
        .substring(reqInfo.getBodyOffset());
    String url = reqInfo.getUrl().toString();

    StringBuilder PoCBuilder = new StringBuilder()
        .append("<!DOCTYPE html>\n")
        .append("<html>\n")
        .append("<body>\n")
        .append("<button onclick='sendRequest()'>Submit request</button>\n")
        .append("<script>\n")
        .append("function sendRequest() {\n")
        .append("  var xhr = new XMLHttpRequest();\n")
        .append("  xhr.open('").append(method).append("', '").append(url).append("');\n")
        .append("  xhr.withCredentials = true;\n");

    if (isJSON(body)) {
      PoCBuilder.append("  xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8');\n")
          .append("  xhr.send('").append(escapeParam(body)).append("');\n");
    }
    PoCBuilder.append("}\n")
        .append("</script>\n")
        .append("</body>\n")
        .append("</html>\n");

    return PoCBuilder.toString();
  }

  private String GenerateFormPoC(IHttpRequestResponse req, IRequestInfo reqInfo) {
    String method = reqInfo.getMethod();
    String url = reqInfo.getUrl().toString();
    String body = new String(req.getRequest(), StandardCharsets.UTF_8)
        .substring(reqInfo.getBodyOffset());

    StringBuilder PoCBuilder = new StringBuilder()
        .append("<html>\n")
        .append("<body>\n")
        .append("<form method=\"").append(method).append("\" action=\"").append(url)
        .append("\">\n");

    String[] params = body.split("&");
    for (String param : params) {
      String[] paramPair = param.split("=");
      String name = paramPair[0];
      String value = "";
      if (paramPair.length == 2) {
        value = paramPair[1];
      }
      PoCBuilder.append("  <input type=\"hidden\" name=\"")
          .append(encodeHTML(burpHelpers.urlDecode(name)))
          .append("\" value=\"").append(encodeHTML(value))
          .append("\">\n");
    }

    PoCBuilder.append("  <input type=\"submit\" value=\"Submit request\">\n")
        .append("</form>\n")
        .append("</body>\n")
        .append("</html>");

    return PoCBuilder.toString();
  }

  private boolean isJSON(String json) {
    // org.json.JSON does not work well on burp...
    if (json.startsWith("{") && json.endsWith("}")) {
      return true;
    }
    return false;
  }

  private String parseHeaderText(List<String> headers) {
    StringBuilder headerBuilder = new StringBuilder();
    for (String header : headers) {
      headerBuilder.append(header)
          .append("\n");
    }
    return headerBuilder.toString();
  }

  private String parseBodyText(byte[] bodyBytes) {
    String req = "";
    try {
      req = new String(bodyBytes, "UTF-8");
      req = req.substring(this.burpHelpers.analyzeResponse(bodyBytes).getBodyOffset());
    } catch (UnsupportedEncodingException e) {
      stderr.println("ERROR: Unsupported Encoding");
    }

    if (req.length() > 0) {
      return new StringBuilder("\n")
          .append(req)
          .toString();
    } else {
      return "";
    }
  }

  private String escapeParam(String escape) {
    return escape.replace("\\", "\\\\")
        .replace("'", "\\'");
  }

  public static String encodeHTML(String encode) {
    return encode.replace("\"", "&quot;");
  }

}
