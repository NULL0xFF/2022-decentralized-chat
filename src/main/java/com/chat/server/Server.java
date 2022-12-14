package com.chat.server;

import com.chat.data.Authentication;
import com.chat.data.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private static final Message AUTH_FAILED = new Message(null, "authentication failed");
    private static final int PORT_NUMBER = 20180;
    private final List<Socket> clientList = new ArrayList<>();
    private final List<Message> messageList = new ArrayList<>();
    private final Map<String, String> whiteList = new HashMap<>();
    private final Map<Socket, ObjectOutputStream> clientOutputStreams = new HashMap<>();
    private final Map<Socket, ObjectInputStream> clientInputStreams = new HashMap<>();
    private ServerSocket serverSocket = null;

    public Server() {
        try {
            System.out.println("initializing socket ...");
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("listening port " + PORT_NUMBER);

        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = this.serverSocket.accept();
                    System.out.println("client " + clientSocket.getInetAddress() + " connected");

                    System.out.println("checking authentication ...");
                    ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    Object object = inputStream.readObject();
                    if (object instanceof Authentication auth) {
                        synchronized (whiteList) {
                            if (whiteList.containsKey(auth.getUsername())) {
                                if (!whiteList.get(auth.getUsername()).equals(auth.getPassword())) {
                                    System.out.println("authentication failed");
                                    outputStream.writeObject(AUTH_FAILED);
                                    inputStream.close();
                                    outputStream.close();
                                    clientSocket.close();
                                    continue;
                                }
                            }
                            // New & Authenticated user
                            outputStream.writeObject(new Message(auth, "authentication successful"));
                            whiteList.put(auth.getUsername(), auth.getPassword());
                            synchronized (clientList) {
                                clientList.add(clientSocket);
                                synchronized (clientOutputStreams) {
                                    clientOutputStreams.put(clientSocket, outputStream);
                                }
                                synchronized (clientInputStreams) {
                                    clientInputStreams.put(clientSocket, inputStream);
                                }
                            }
                        }
                    }
                    history(clientSocket);
                    new Thread(() -> receive(clientSocket)).start();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void history(Socket clientSocket) {
        ObjectOutputStream outputStream = clientOutputStreams.get(clientSocket);
        for (Message message : messageList) {
            try {
                outputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void receive(Socket clientSocket) {
        try {
            ObjectInputStream inputStream = clientInputStreams.get(clientSocket);
            while (true) {
                Object object = inputStream.readObject();
                if (object instanceof Message message) {
                    messageList.add(message);
                    new Thread(() -> broadcast(message)).start();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            synchronized (clientList) {
                synchronized (clientInputStreams) {
                    try {
                        clientInputStreams.remove(clientSocket).close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                synchronized (clientOutputStreams) {
                    try {
                        clientOutputStreams.remove(clientSocket).close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                clientList.remove(clientSocket);
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void broadcast(Message message) {
        synchronized (clientOutputStreams) {
            for (ObjectOutputStream outputStream : clientOutputStreams.values()) {
                if (outputStream != null) {
                    try {
                        outputStream.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
