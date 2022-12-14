package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static final int PORT_NUMBER = 20180;
    private final List<Socket> clientList = new ArrayList<>();
    private ServerSocket serverSocket = null;

    public ChatServer() {
        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
        } catch (IOException e) {
            System.err.println("다음의 포트 번호에 연결할 수 없습니다: " + PORT_NUMBER);
            e.printStackTrace();
            System.exit(1);
        }

        Thread clientConnectionThread = new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = this.serverSocket.accept();
                    System.out.println("client " + clientSocket.getInetAddress() + " connected");
                    synchronized (clientSocket) {
                        clientList.add(clientSocket);
                    }
                    new Thread(() -> receiveThread(clientSocket)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        clientConnectionThread.start();
        try {
            clientConnectionThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void receiveThread(Socket clientSocket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

            String clientInput = null;
            while ((clientInput = dataInputStream.readUTF()) != null) {
                System.out.println(clientInput);
                if (clientInput.equals("/exit")) {
                    break;
                }
            }

            dataInputStream.close();
            synchronized (clientList) {
                clientList.remove(clientSocket);
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
