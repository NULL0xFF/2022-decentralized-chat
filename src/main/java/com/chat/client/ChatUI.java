package com.chat.client;

import com.chat.data.Authentication;
import com.chat.data.Message;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatUI {

    private final Authentication authentication;
    private final Client client;
    private JPanel contentPane;
    private JTextArea chatList;
    private JTextArea inputArea;
    private JButton sendButton;

    public ChatUI(String host, int port, Authentication auth) {
        this.authentication = auth;
        JFrame frame = new JFrame();
        frame.setContentPane(contentPane);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.disconnect();
            }
        });
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Action sendAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSend();
            }
        };

        ((DefaultCaret) chatList.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        inputArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "send-message");
        inputArea.getInputMap().put(KeyStroke.getKeyStroke('\n', InputEvent.CTRL_DOWN_MASK), "insert-break");
        inputArea.getActionMap().put("send-message", sendAction);
        sendButton.addActionListener(sendAction);

        client = new Client(host, port, authentication);

        doConnect();
    }

    private void doConnect() {
        chatList.append("connecting to server...\n");
        if (client.connect(this)) {
            chatList.append("connected to server\n");
            inputArea.setEnabled(true);
            sendButton.setEnabled(true);
        } else {
            chatList.append("failed to connect\n");
        }
    }

    private void doSend() {
        String inputString = inputArea.getText().replaceAll("\n", " ");
        inputArea.setText("");
        inputArea.requestFocus();

        Message message = new Message(authentication, inputString);
        client.send(message);
    }

    public void doReceive(Message data) {
        chatList.append(data.toString() + "\n");
    }

}
