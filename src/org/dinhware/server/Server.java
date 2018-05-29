package org.dinhware.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server implements Runnable {

    static final int NORMAL_MESSAGE = 1;
    static final int NAME_HEADER = 2;
    static final int NAME_REQUEST = 3;
    static final int NAME_OK = 4;
    static final int NAME_TAKEN = 5;
    static final int CONNECT = 7;
    static final int GENERIC_PACKAGE = 0xFE;
    static final int CON_RESET = 8;
    static final int DISCONNECT = -1;

    static final int SUDO_REQUEST = 9;
    static final int MUTE_USER = 10;
    static final int KICK_USER = 11;

    static ClientListener clients[] = new ClientListener[50];
    static HashMap<Integer, String> connected = new HashMap<>();
    private ServerSocket server = null;
    private Thread thread = null;
    static int clientCount = 0;

    boolean isUserConnected(String name) {
        for (int i = 0; i < clientCount; i++) {
            if (connected.containsKey(clients[i].getID())) {
                if (connected.get(clients[i].getID()).equalsIgnoreCase(name))
                    return true;
            }
        }
        return false;
    }

    private Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("= SERVER STARTED, USING PORT: " + server.getLocalPort());
            start();
        } catch (IOException ioe) {
            System.out.println("= CANT BIND TO PORT " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("= WAITING FOR A NEW CLIENT");
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("= SERVER ACCEPT ERROR: " + ioe);
                stop();
            }
        }
    }

    private void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    private void stop() {
        if (thread != null) {
            thread = null;
        }
    }

    private static int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    synchronized void handleNormalMessage(int ID, String input) {
        for (int i = 0; i < clientCount; i++) {
            try {
                if (i == findClient(ID)) {
                    continue;
                }
                clients[i].getOut().writeByte(NORMAL_MESSAGE);
                clients[i].getOut().writeByte(NAME_HEADER);
                clients[i].getOut().writeUTF(protect(connected.get(ID), true));
                clients[i].getOut().writeUTF(input);
                clients[i].getOut().flush();
            } catch (IOException e) {
                System.out.println("= " + ID + " ERROR SENDING: " + e.getMessage());
                remove(ID);
                stop();
            }
        }

    }


    static String protect(String utf, boolean encrypt) {
        StringBuilder buffer = new StringBuilder(utf);
        int mode = encrypt ? 2 : -2;
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, (char) (buffer.charAt(i) + mode));
        }
        return buffer.toString();
    }


    static synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ClientListener toTerminate = clients[pos];
            System.out.println("= REMOVING CLIENT THREAD " + ID + " at " + pos);
            if (pos < clientCount - 1)
                System.arraycopy(clients, pos + 1, clients, pos + 1 - 1, clientCount - pos + 1);
            clientCount--;
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("= ERROR CLOSING THREAD: " + ioe);
            }
            toTerminate.stop();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("= CLIENT ACCEPTED: " + socket);
            clients[clientCount] = new ClientListener(this, socket);
            try {
                clients[clientCount].open();
                new Thread(clients[clientCount]).start();
                clientCount++;
            } catch (IOException ioe) {
                System.out.println("= ERROR OPENING THREAD: " + ioe);
            }
        } else
            System.out.println("= CLIENT REFUSED MAXIMUM " + clients.length + " REACHED");
    }

    public static void main(String args[]) {
        new Server(6969);
    }
}