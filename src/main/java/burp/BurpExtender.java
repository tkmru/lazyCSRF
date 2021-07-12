package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        stdout.println("INFO : Hello from LazyCSRF");
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
                        GeneratePoC(menuInvocation.getSelectedMessages());
                        CSRFPoCWindow view = new CSRFPoCWindow("LazyCSRF");
                        view.setVisible();
                        view.setCSRFPoCHTML("aaaaaaaaa");
                   }
               }
            });
            menuList.add(GeneratePocButton);
        }
        
        return menuList;
    }
    
    private void GeneratePoC(IHttpRequestResponse[] messages)
    {
        String firstHalfPoCTemplate = new StringBuilder()
                .append("<html>\n")
                .append("<body>\n")
                .append("  <script>history.pushState('', '', '/')</script>\n")
                .append("  <form action=\"target-url\">\n")
                .toString();

        String latterHalfPoCTemplate = new StringBuilder()
                .append("    <input type=\"submit\" value=\"Submit request\" />\n")
                .append("  </form>\n")
                .append("</body>\n")
                .append("</html>\n")
                .toString();
        
        for(int i=0; i < messages.length; i++) {
            stdout.println(messages[i].getHttpService().getProtocol());
            stdout.println(messages[i].getHttpService().getHost());
            stdout.println(messages[i].getHttpService().getPort());
            stdout.println(messages[i].getRequest());
        }
    }
}
