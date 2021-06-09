package burp;

import java.io.PrintWriter;

public class BurpExtender implements IBurpExtender {

    public void registerExtenderCallbacks(IBurpExtenderCallbacks iBurpExtenderCallbacks) {
        iBurpExtenderCallbacks.setExtensionName("LazyCSRF");

        PrintWriter stdout = new PrintWriter(iBurpExtenderCallbacks.getStdout(), true);
        stdout.println("INFO : Hello, Burp Suite");

        PrintWriter stderr = new PrintWriter(iBurpExtenderCallbacks.getStderr(), true);
        stderr.println("ERROR : Hello, Burp Suite");

        iBurpExtenderCallbacks.issueAlert("Burp Suite Alerts");

        throw new RuntimeException("Burp Suite exceptions");
    }
}