package com.chat.server;

import com.chat.data.Authentication;
import com.chat.data.Message;

import java.io.*;
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
    private final List<String> adminList = new ArrayList<>();
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
                    ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    Object object = inputStream.readObject();
                    if (object instanceof Authentication auth) {
                        synchronized (whiteList) {
                            if (whiteList.containsKey(auth.getUsername())) {
                                if (!whiteList.get(auth.getUsername()).equals(auth.getPassword())) {
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

        startConsole();
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
                    if (message.isCommand()) {
                        doCommand(clientSocket, message);
                    } else {
                        messageList.add(message);
                    }
                    new Thread(() -> broadcast(message)).start();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            removeClient(clientSocket);
        }
    }

    private void startConsole() {
        System.out.println("server console");
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                String input = console.readLine();
                if (input.equals("stop")) {
                    clientInputStreams.forEach((socket, stream) -> {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    clientOutputStreams.forEach((socket, stream) -> {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    for (Socket socket : clientList) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.exit(0);
                } else if (input.startsWith("user ")) {
                    String userCommand = input.substring(5);
                    if (userCommand.startsWith("add ")) {
                        addUser(userCommand.substring(4).split(" "));
                    }
                    if (userCommand.startsWith("remove ")) {
                        removeUser(userCommand.substring(7));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUser(String[] identification) {
        Authentication auth = new Authentication(identification[0], identification[1]);
        whiteList.put(auth.getUsername(), auth.getPassword());
    }

    private void removeUser(String username) {
        whiteList.remove(username);
    }

    private void doCommand(Socket clientSocket, Message message) {
        String command = message.getMessage().substring(1);
        if (command.startsWith("admin ")) {
            if (isAdmin(message.getAuthentication())) {
                doAdminAction(message);
            }
        } else if (command.equals("leave")) {
            removeClient(clientSocket);
            synchronized (whiteList) {
                whiteList.remove(message.getAuthentication().getUsername());
            }
        }
    }

    private void removeClient(Socket clientSocket) {
        synchronized (clientList) {
            synchronized (clientInputStreams) {
                try {
                    clientInputStreams.remove(clientSocket).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            synchronized (clientOutputStreams) {
                try {
                    clientOutputStreams.remove(clientSocket).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clientList.remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isAdmin(Authentication authentication) {
        return adminList.contains(authentication.getUsername());
    }

    private void doAdminAction(Message message) {
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
