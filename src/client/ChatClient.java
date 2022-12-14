package client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ChatClient {

    private Socket socket;
    private Thread sendThread, receiveThread;

    public ChatClient() {
    }

    private void connect(String url, int port) {
        try {
            socket = new Socket(url, port);
        } catch (IOException e) {
            System.err.println("다음의 서버에 접속할 수 없습니다: " + url + ":" + port);
            e.printStackTrace();
            System.exit(1);
        }

        receiveThread = new Thread(this::receive);
        sendThread = new Thread(this::send);
        receiveThread.start();
        sendThread.start();
        try {
            receiveThread.join();
        } catch (InterruptedException e) {
            receiveThread.interrupt();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void receive() {
        while (true) {
            try {
                String receiveData = new DataInputStream(socket.getInputStream()).readUTF();
                System.out.println("Server: " + receiveData);
            } catch (SocketException | EOFException e) {
                e.printStackTrace();
                sendThread.interrupt();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void send() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String inputString = null;
        while (true) {
            try {
                inputString = reader.readLine();
                new DataOutputStream(socket.getOutputStream()).writeUTF(inputString);
                if (inputString.equals("/exit")) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
