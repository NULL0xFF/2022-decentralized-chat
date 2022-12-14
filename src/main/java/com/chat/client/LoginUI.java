package com.chat.client;

import com.chat.data.Authentication;

import javax.swing.*;

public class LoginUI {

    private final JFrame frame = new JFrame();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel contentPane;

    public LoginUI() {
        frame.setContentPane(contentPane);
        frame.getRootPane().setDefaultButton(loginButton);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        loginButton.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        new ChatListUI(new Authentication(usernameField.getText(), String.valueOf(passwordField.getPassword())));
        frame.dispose();
    }

}
