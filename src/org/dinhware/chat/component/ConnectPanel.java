package org.dinhware.component;

import org.dinhware.Callback;
import org.dinhware.action.Connect;

import javax.swing.*;
import java.awt.*;

public class ConnectPanel extends JPanel {

    private Connect action;

    public ConnectPanel(Callback callback) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(360, 40));

        JPanel top = new JPanel(new FlowLayout());
        add(top, BorderLayout.CENTER);

        JLabel connectionStatus = new JLabel("", SwingConstants.CENTER);
        add(connectionStatus, BorderLayout.SOUTH);

        JTextField name = new JTextField(System.getProperty("user.name"), 10);
        top.add(name);

        JTextField ip = new JTextField("194.135.95.21", 10);
        top.add(ip);

        action = new Connect(callback, connectionStatus, ip, name);
        JButton connect = new JButton(action);
        top.add(connect);

        connect.setFocusPainted(false);
        connect.setPreferredSize(new Dimension(100, 19));

    }

    public Connect getAction() {
        return action;
    }
}
