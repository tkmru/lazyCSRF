package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import javax.swing.JMenuItem;
import java.util.ArrayList;
import javax.swing.JOptionPane;


public class BurpExtender implements IBurpExtender, IContextMenuFactory
{

    private IContextMenuInvocation mInvocation;
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
        mInvocation = invocation;
        
        if(mInvocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_PROXY_HISTORY) {
            JMenuItem markScan = new JMenuItem("Generate Better CSRF PoC");
            markScan.addActionListener(new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent arg0) {
                   if(arg0.getActionCommand().equals("Generate Better CSRF PoC")) {
                      GeneratePoC(mInvocation.getSelectedMessages());
                   }
               }
            });
            menuList.add(markScan);
        }
        
        return menuList;
    }
    
    private void GeneratePoC(IHttpRequestResponse[] messages)
    {
        for(int i=0; i < messages.length; i++) {
            stdout.println(messages[i].getHttpService().getProtocol());
            stdout.println(messages[i].getHttpService().getHost());
            stdout.println(messages[i].getHttpService().getPort());
            stdout.println(messages[i].getRequest());
        }
    }
}
