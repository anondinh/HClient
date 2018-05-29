package org.dinhware.chat;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static org.dinhware.chat.Server.*;

public class ClientListener implements Runnable {
    private int ID;
    private Server server;
    private Socket socket;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;

    ClientListener(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        ID = socket.getPort();
    }

    DataOutputStream getOut() {
        return streamOut;
    }

    int getID() {
        return ID;
    }

    public void run() {
        final InetAddress address = socket.getInetAddress();
        System.out.println("SERVER THREAD " + ID + " RUNNING");
        String content;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                switch (streamIn.read()) {
                    case SUDO_REQUEST:
                        content = streamIn.readUTF();
                        notifyAllUsers(content, SUDO_REQUEST);
                        break;
                    case MUTE_USER:
                        content = streamIn.readUTF();
                        notifyAllUsers(content, MUTE_USER);
                        break;
                    case KICK_USER:
                        content = streamIn.readUTF();
                        notifyAllUsers(content, KICK_USER);
                        break;
                    case NORMAL_MESSAGE:
                        content = streamIn.readUTF();
                        String decrypted = Server.protect(content, false);
                        System.out.println("< " + address + "[" + decrypted + "]");
                        server.handleNormalMessage(ID, content);
                        break;
                    case NAME_REQUEST:
                        String name = Server.protect(streamIn.readUTF(), false);
                        System.out.println("< NAME REQUEST " + name);
                        if (server.isUserConnected(name)) {
                            streamOut.write(NAME_TAKEN);
                            System.out.println("> NAME TAKEN " + name);
                        } else {
                            System.out.println("> NAME OK " + name);
                            streamOut.write(NAME_OK);
                            streamOut.write(NAME_OK);
                            Server.connected.put(ID, name);
                            for (int i = 0; i < Server.clientCount; i++) {
                                if (Server.clients[i].getID() == ID) {
                                    for (String user : Server.connected.values()) {
                                        if (user == null) {
                                            continue;
                                        }
                                        streamOut.write(NAME_HEADER);
                                        streamOut.writeUTF(Server.protect(user, true));
                                    }
                                    streamOut.write(GENERIC_PACKAGE);
                                } else {
                                    System.out.println("> SENDING NAME " + name);
                                    Server.clients[i].streamOut.write(CONNECT);
                                    Server.clients[i].streamOut.write(NAME_HEADER);
                                    Server.clients[i].streamOut.writeUTF(Server.protect(name, true));
                                    Server.clients[i].streamOut.flush();
                                }
                            }
                        }
                        streamOut.flush();
                        break;
                    case GENERIC_PACKAGE:
                        break;
                    case DISCONNECT:
                        System.out.println("> " + address + " DISCONNECTED");
                        streamOut.writeByte(DISCONNECT);
                        streamOut.flush();
                        connectionReset();
                    default:
                        System.out.println("< " + ID + " UNKNOWN ERROR");
                        break;
                }
            } catch (IOException ioe) {
                System.out.println("< " + ID + " ERROR READING: " + ioe.getMessage());
                connectionReset();
            }
        }
    }

    private void notifyAllUsers(String content, int... bytes) throws IOException {
        for (int i = 0; i < Server.clientCount; i++) {
            for (int code : bytes) {
                Server.clients[i].streamOut.write(code);
            }
            Server.clients[i].streamOut.writeUTF(Server.protect(content, true));
            Server.clients[i].streamOut.flush();
        }
    }

    private void connectionReset() {
        for (int i = 0; i < Server.clientCount; i++) {
            if (Server.clients[i].getID() == ID) {
                continue;
            }
            try {
                System.out.println("< SERVER: " + Server.connected.get(ID));
                if (Server.connected.get(ID) == null) continue;
                Server.clients[i].streamOut.write(CON_RESET);
                Server.clients[i].streamOut.writeUTF(Server.protect(Server.connected.get(ID), true));
                Server.clients[i].streamOut.flush();
            } catch (IOException e) {
                System.out.println("< " + ID + " ERROR WRITING: " + e.getMessage());
                e.printStackTrace();
            }
        }
        Server.connected.remove(ID);
        Server.remove(ID);
        stop();
    }

    void stop() {
        Thread.currentThread().interrupt();
    }

    void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (streamIn != null) {
            streamIn.close();
        }
        if (streamOut != null) {
            streamOut.close();
        }
    }
}