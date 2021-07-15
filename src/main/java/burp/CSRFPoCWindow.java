package burp;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CSRFPoCWindow {

    private JButton copyHTMLButton;
    private JLabel csrfPoCLabel;
    private JLabel requestLabel;
    private JTextPane requestTextPane;
    private JTextPane csrfPoCTextPane;
    private JFrame frame;
    private JPanel mainPanel;
    private JScrollPane mainScrollPane;
    private JScrollPane requestScrollPane;
    private JScrollPane csrfPoCScrollPane;

    public CSRFPoCWindow(String title) {
        initialize(title);
    }

    private void initialize(String title) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setBounds(100, 100, 775, 900);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainScrollPane = new JScrollPane(getMainPanel());
        frame.getContentPane().add(mainScrollPane);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setPreferredSize(new Dimension(750, 875));
            mainPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(11, 4, 11, 8));

            requestLabel = new JLabel("Request to: ");
            requestLabel.setAlignmentX(0.0f);

            requestTextPane = new JTextPane();
            requestTextPane.getDocument().putProperty("name", "Request");
            requestScrollPane = new JScrollPane(requestTextPane);
            requestScrollPane.setBorder(new LineBorder(Color.GRAY, 1, true));
            requestScrollPane.setPreferredSize(new Dimension(730, 250));
            requestScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 250));
            requestScrollPane.setAlignmentX(0.0f);

            csrfPoCLabel = new JLabel("CSRF HTML PoC:");
            csrfPoCLabel.setAlignmentX(0.0f);

            csrfPoCTextPane = new JTextPane();
            csrfPoCTextPane.getDocument().putProperty("name", "CSRF PoC");
            csrfPoCScrollPane = new JScrollPane(csrfPoCTextPane);
            csrfPoCScrollPane.setBorder(new LineBorder(Color.GRAY, 1, true));
            csrfPoCScrollPane.setAlignmentX(0.0f);

            copyHTMLButton = new JButton("Copy PoC HTML");
            copyHTMLButton.setAlignmentX(0.0f);

            mainPanel.add(requestLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(10,8)));
            mainPanel.add(requestScrollPane);
            mainPanel.add(Box.createRigidArea(new Dimension(10,8)));
            mainPanel.add(csrfPoCLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(10,8)));
            mainPanel.add(csrfPoCScrollPane);
            mainPanel.add(Box.createRigidArea(new Dimension(10,8)));
            mainPanel.add(copyHTMLButton);
            mainPanel.revalidate();
        }
        return mainPanel;
    }

    public void setVisible() {
        frame.setVisible(true);
    }

    public void setRequest(String request) {
        requestTextPane.setText(request);
    }

    public void setRequestLabel(String url) {
        requestLabel.setText(new StringBuilder()
                .append("Request to: ")
                .append(url)
                .toString()
        );
    }

    public void setCSRFPoCHTML(String html) {
        csrfPoCTextPane.setText(html);
    }
}