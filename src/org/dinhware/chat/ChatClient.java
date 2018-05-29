package org.dinhware;

import org.dinhware.component.ChatPanel;
import org.dinhware.component.ConnectPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatClient implements Callback, ShutdownInterface {

    public static final int NORMAL_MESSAGE = 1;
    public static final int NAME_REQUEST = 3;
    public static final int NAME_OK = 4;
    public static final int NAME_TAKEN = 5;
    public static final int NAME_NOT_ALLOWED = 6;

    static final int NAME_HEADER = 2;
    static final int CONNECT = 7;
    static final int CONNECTION_RESET = 8;
    static final int DISCONNECT = 0xFF;

    public static final int SUDO_REQUEST = 9;
    public static final int MUTE_USER = 10;
    public static final int KICK_USER = 11;

    public static boolean isSuperUser;

    private static CardLayout cardLayout = new CardLayout();
    private static ListenerThread thread;
    private static Container container;
    private static JFrame frame;

    public static volatile String username = System.getProperty("user.name");

    private ChatClient(JFrame frame) {
        container = frame.getContentPane();
        container.setLayout(cardLayout);
        ConnectPanel connectPanel = new ConnectPanel(this);
        container.add(connectPanel, "connect");

        cardLayout.show(container, "connect");

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (thread != null) {
                    try {
                        thread.disconnect();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                shutdown();
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);

        /*
         * Automatically reconnect
         * connectPanel.getAction().actionPerformed(null);
         */
    }

    public static void main(String[] args) {
        new ChatClient(frame = new JFrame("Client"));
    }

    @Override
    public void setChat(DataOutputStream stream, String address, String username) {
        container.removeAll();

        container.setPreferredSize(new Dimension(600, 220));
        container.add(new ChatPanel(stream, address, ChatClient.username = username), "chat");

        container.revalidate();

        frame.pack();
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(300, 165));
        frame.setLocationRelativeTo(null);
        frame.repaint();
    }

    @Override
    public void initializeListener(ListenerThread thread) {
        ChatClient.thread = thread;
        ChatClient.thread.setShutdownInterface(this);
        ChatClient.thread.start();
    }

    @Override
    public void shutdown() {
        if (thread != null) {
            thread.interrupt();
        }
    }

}
