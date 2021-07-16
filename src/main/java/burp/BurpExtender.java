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

    private static String JSON_CSRF_MENU_NAME = "Generate JSON CSRF PoC with Ajax";
    private static String PUT_DELETE_CSRF_MENU_NAME = "Generate DELETE/PUT CSRF PoC with Ajax";
    private static String NORMAL_CSRF_MENU_NAME = "Generate Normal CSRF PoC with Form";

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.burpCallbacks = callbacks;
        this.burpCallbacks.setExtensionName("LazyCSRF");
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
        if(menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY) {
            JMenuItem GeneratePocButton = new JMenuItem(JSON_CSRF_MENU_NAME);
            GeneratePocButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if(arg0.getActionCommand().equals(JSON_CSRF_MENU_NAME)) {
                        IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
                        for (IHttpRequestResponse req : selectedMessages) {
                            IRequestInfo reqInfo = iexHelpers.analyzeRequest(req);
                            CSRFPoCWindow view = new CSRFPoCWindow("LazyCSRF");
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
            menuList.add(GeneratePocButton);
        }
        return menuList;
    }
    
    private String GenerateJSONPoC(IHttpRequestResponse req, IRequestInfo reqInfo)
    {
        String method = reqInfo.getMethod();
        String body = this.iexHelpers.bytesToString(req.getRequest()).substring(reqInfo.getBodyOffset());
        String url = reqInfo.getUrl().toString();

        StringBuilder PoCBuilder = new StringBuilder()
                .append("<html>\n")
                .append("<body>\n")
                .append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js\"></script>\n")
                .append("<script>\n")
                .append("$.ajax({\n")
                .append("  url: '")
                .append(url)
                .append("',\n")
                .append("  type: '")
                .append(method)
                .append("',\n")
                .append("  contentType: 'application/json; charset=utf-8',\n")
                .append("  xhrFields: {\n")
                .append("    withCredentials: true\n")
                .append("  },\n")
                .append("  crossDomain: true,\n");

        if (isJSON(body)) {
            PoCBuilder.append("  contentType: 'application/json; charset=utf-8',\n")
                    .append("  data: '")
                    .append(escapeParam(body));
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
            stringBuilder.append(header);
            stringBuilder.append(System.lineSeparator());
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
            req = System.lineSeparator() + req;
            return req;
        } else {
            return "";
        }
    }

    private String escapeParam(String escape) {
        return escape.replace("\\", "\\\\")
                .replace("'", "\\'");
    }

}
