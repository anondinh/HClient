package org.dinhware;

import org.dinhware.action.Message;
import org.dinhware.component.ChatPanel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ListenerThread extends Thread {
    private final Socket connection;
    private final DataInputStream in;
    private final DataOutputStream out;

    private ShutdownInterface shutdownInterface;

    public ListenerThread(Socket connection, DataOutputStream out, DataInputStream in) {
        this.connection = connection;
        this.out = out;
        this.in = in;
    }

    void setShutdownInterface(ShutdownInterface shutdownInterface) {
        this.shutdownInterface = shutdownInterface;
    }

    void disconnect() throws IOException {
        if (out != null) {
            out.writeByte(ChatClient.DISCONNECT);
            out.flush();
            out.close();
        }
        if (connection != null) {
            connection.close();
        }
        if (in != null) {
            in.close();
        }
        interrupt();
    }


    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                String utf;
                switch (in.read()) {
                    case ChatClient.SUDO_REQUEST:
                        utf = Message.protect(in.readUTF(), false);
                        if (ChatClient.username.equals(utf)) {
                            ChatClient.isSuperUser = true;
                        }
                        ChatPanel.addContent(utf + " is now a super user", false);
                        break;
                    case ChatClient.KICK_USER:
                        utf = Message.protect(in.readUTF(), false);
                        if (ChatClient.username.equals(utf)) {
                            System.exit(0);
                        } else {
                            ChatPanel.addContent(utf + " has been kicked by a super user", false);
                        }
                        break;
                    case ChatClient.MUTE_USER:
                        utf = Message.protect(in.readUTF(), false);
                        if (ChatClient.username.equals(utf)) {
                            ChatPanel.toggle();
                        }
                        break;
                    case ChatClient.CONNECTION_RESET:
                        utf = Message.protect(in.readUTF(), false);
                        ChatPanel.addContent(utf + " has disconnected", false);
                        break;
                    case ChatClient.DISCONNECT:
                        shutdownInterface.shutdown();
                        break;
                    case ChatClient.NORMAL_MESSAGE:
                        if (in.read() != ChatClient.NAME_HEADER) {
                            shutdownInterface.shutdown();
                        } else {
                            utf = Message.protect(in.readUTF(), false);
                            ChatPanel.addMessage(Message.protect(in.readUTF(), false), utf);
                        }
                        break;
                    case ChatClient.NAME_OK:
                        if (in.read() != ChatClient.NAME_HEADER) {
                            shutdownInterface.shutdown();
                        } else {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Currently connected users:");
                            do {
                                utf = Message.protect(in.readUTF(), false);
                                builder.append(utf).append(", ");
                            } while (in.read() == ChatClient.NAME_HEADER);
                            builder.setLength(builder.length() - 2);
                            builder.append("\n");
                            ChatPanel.addContent(builder.toString(), false);
                        }
                        break;
                    case ChatClient.CONNECT:
                        if (in.read() != ChatClient.NAME_HEADER) {
                            shutdownInterface.shutdown();
                        } else {
                            utf = Message.protect(in.readUTF(), false);
                            ChatPanel.addContent(utf + " has connected", false);
                        }
                        break;
                    default:
                        System.out.println("Unknown code");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                shutdownInterface.shutdown();
                System.exit(0);
            }
        }
        shutdownInterface.shutdown();
    }
}