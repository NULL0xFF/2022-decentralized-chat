package com.chat.client;

import com.chat.data.Authentication;

import javax.swing.*;
import java.awt.*;

public class ChatListUI {

    private final JFrame frame = new JFrame();
    private final Authentication authentication;
    private JPanel contentPane;
    private JTextField hostField;
    private JTextField portField;
    private JButton addButton;
    private JPanel entryPanel;

    public ChatListUI(Authentication auth) {
        authentication = auth;

        frame.setContentPane(contentPane);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        addButton.addActionListener(e -> doAdd());
    }

    private void doAdd() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());

            if (port < 0 || port > 65536) {
                return;
            }

            JPanel panel = new JPanel(new BorderLayout(0, 0));
            JButton connectButton = new JButton("Connect");

            Dimension panelDimension = new Dimension(250, 40);
            panel.setMinimumSize(panelDimension);
            panel.setMaximumSize(panelDimension);
            panel.setPreferredSize(new Dimension(-1, 40));
            connectButton.addActionListener(e -> new ChatUI(host, port, authentication));

            panel.add(new JLabel(host + ":" + port), BorderLayout.CENTER);
            panel.add(connectButton, BorderLayout.EAST);

            entryPanel.add(panel);

            frame.pack();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
    }
}
