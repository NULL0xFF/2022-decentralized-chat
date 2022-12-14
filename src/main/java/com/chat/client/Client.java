package com.chat.client;

import com.chat.data.Authentication;
import com.chat.data.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    private final String host;
    private final int port;
    private final Authentication authentication;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread thread;

    public Client(String host, int port, Authentication auth) {
        this.host = host;
        this.port = port;
        this.authentication = auth;
    }

    public boolean connect(ChatUI ui) {
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(authentication);
            Object object = inputStream.readObject();
            if (!(object instanceof Message message)) {
                return false;
            } else {
                if (message.getAuthentication() == null) {
                    ui.doReceive(message);
                    return false;
                }
            }
            receive(ui);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void receive(ChatUI ui) {
        thread = new Thread(() -> {
            while (true) {
                try {
                    Object object = inputStream.readObject();
                    if (object instanceof Message message) {
                        ui.doReceive(message);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void send(Message message) {
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (thread != null) {
            thread.interrupt();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}