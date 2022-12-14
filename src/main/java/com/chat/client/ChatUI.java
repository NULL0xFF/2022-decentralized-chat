package com.chat.client;

import com.chat.data.Authentication;
import com.chat.data.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ChatUI {

    private final Authentication authentication;
    private final Client client;
    private JPanel contentPane;
    private JList<Message> chatList;
    private DefaultListModel<Message> model;
    private JTextArea inputArea;
    private JButton sendButton;
    private JScrollPane chatPane;

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

        inputArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "send-message");
        inputArea.getInputMap().put(KeyStroke.getKeyStroke('\n', InputEvent.CTRL_DOWN_MASK), "insert-break");
        inputArea.getActionMap().put("send-message", sendAction);
        sendButton.addActionListener(sendAction);

        client = new Client(host, port, authentication);

        doConnect();
    }

    private void doConnect() {
        Message message = new Message(null, "connecting to server...");
        model.addElement(message);
        if (client.connect(this)) {
            model.removeElement(message);
            model.addElement(new Message(null, "connected to server"));
            inputArea.setEnabled(true);
            sendButton.setEnabled(true);
        } else {
            model.addElement(new Message(null, "failed to connect"));
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
        model.addElement(data);
        doUpdate();
    }

    private void doUpdate() {
        chatPane.getVerticalScrollBar().setValue(chatPane.getVerticalScrollBar().getMaximum());
    }

    private void createUIComponents() {
        model = new DefaultListModel<>();
        chatList = new JList<>(model);
    }
}
