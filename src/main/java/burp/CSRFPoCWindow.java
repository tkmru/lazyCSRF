package burp;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Dimension;

public class CSRFPoCWindow {

    private JButton copyHTMLButton;
    private JLabel csrfPoCLabel;
    private JTextPane csrfPoCTextPane;
    private JFrame frame;
    private JPanel mainPanel;
    private JScrollPane mainScrollPane;

    public CSRFPoCWindow(String title) {
        initialize(title);
    }

    private void initialize(String title) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setBounds(100, 100, 675, 825);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainScrollPane = new JScrollPane(getMainPanel());
        frame.getContentPane().add(mainScrollPane);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setPreferredSize(new Dimension(650, 800));
            mainPanel.setLayout(null);

            csrfPoCLabel = new JLabel("CSRF HTML PoC:");
            csrfPoCLabel.setBounds(12, 10, 150, 15);

            csrfPoCTextPane = new JTextPane();
            csrfPoCTextPane.getDocument().putProperty("name", "CSRF PoC");
            csrfPoCTextPane.setBounds(12, 35, 650, 200);

            copyHTMLButton = new JButton("Copy PoC HTML");
            copyHTMLButton.setBounds(12, 765, 150, 25);

            mainPanel.add(csrfPoCLabel);
            mainPanel.add(csrfPoCTextPane);
            mainPanel.add(copyHTMLButton);

        }
        return mainPanel;
    }

    public void setVisible() {
        frame.setVisible(true);
    }

    public void setCSRFPoCHTML(String html) {
        if(html == null) return;
        csrfPoCTextPane.setText(html);
    }
}