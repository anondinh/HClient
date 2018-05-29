package org.dinhware.action;

import org.dinhware.Callback;
import org.dinhware.ListenerThread;
import org.dinhware.ChatClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connect extends AbstractAction {

    private Callback callback;
    private JTextField address, username;
    private JLabel status;

    public Connect(Callback callback, JLabel status, JTextField address, JTextField username) {
        super("connect");
        this.callback = callback;
        this.username = username;
        this.address = address;
        this.status = status;
    }

    public void actionPerformed(ActionEvent e) {
        if (address == null) return;
        InetSocketAddress address = new InetSocketAddress(this.address.getText(), 6969);
        status.setText("Cant connect to " + this.address.getText());
        try {
            Socket connection = new Socket(address.getAddress(), address.getPort());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            DataInputStream in = new DataInputStream(connection.getInputStream());
            out.write(ChatClient.NAME_REQUEST);
            out.writeUTF(Message.protect(username.getText(), true));
            out.flush();

            int instruction = in.read();
            switch (instruction) {
                case ChatClient.NAME_OK:
                    callback.setChat(out, this.address.getText(), username.getText());
                    callback.initializeListener(new ListenerThread(connection, out, in));
                    break;
                case ChatClient.NAME_TAKEN:
                case ChatClient.NAME_NOT_ALLOWED:
                    status.setText("Name already taken");
                    connection.close();
                    out.close();
                    in.close();
                    break;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
