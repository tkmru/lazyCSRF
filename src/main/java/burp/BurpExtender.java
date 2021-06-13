package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BurpExtender implements IBurpExtender, IContextMenuFactory, ITab
{

    private IContextMenuInvocation menuInvocation;
    private IBurpExtenderCallbacks burpCallbacks;
    private PrintWriter stdout;

    private JPanel jPanel1;
    private JButton jButton1;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        this.burpCallbacks = callbacks;
        this.burpCallbacks.setExtensionName("LazyCSRF");
        this.burpCallbacks.registerContextMenuFactory(this);

        stdout = new PrintWriter(this.burpCallbacks.getStdout(), true);
        stdout.println("INFO : Hello from LazyCSRF");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                 //Create a JPanel
                 jPanel1 = new JPanel();
                 jButton1 = new JButton("click me");
 
                 // add the button to the panel
                 jPanel1.add(jButton1);
 
                 //Customized UI components
                 callbacks.customizeUiComponent(jPanel1);
                 //Add custom tabs to Burp UI
                 callbacks.addSuiteTab(BurpExtender.this);
            }
        });
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
                   }
               }
            });
            menuList.add(GeneratePocButton);
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

    @Override
    public String getTabCaption() {
        // Return the title of the custom tab page
        return "LazyCSRF PoC";
    }
 
    @Override
    public Component getUiComponent() {
        // Return the component object of the panel in the custom tab
        return jPanel1;
    }
}
