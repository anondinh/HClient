package org.dinhware.action;

import org.dinhware.ChatClient;
import org.dinhware.Emote;
import org.dinhware.WebColor;
import org.dinhware.component.ChatPanel;
import org.dinhware.special.Command;
import org.dinhware.special.KickCommand;
import org.dinhware.special.MuteCommand;
import org.dinhware.special.SudoCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Message extends AbstractAction {

    private ChatPanel chatPanel;
    private DataOutputStream out;

    private final static WebColor[] colors = WebColor.values();
    private final static Emote[] emotes = Emote.values();

    private static Map<String, Command> map = new HashMap<>();

    static {
        map.put("sudo", new SudoCommand());
        map.put("mute", new MuteCommand());
        map.put("kick", new KickCommand());
    }

    public Message(ChatPanel chatPanel, DataOutputStream out) {
        this.chatPanel = chatPanel;
        this.out = out;
    }

    public static String protect(String utf, boolean encrypt) {
        StringBuilder buffer = new StringBuilder(utf);
        int mode = encrypt ? 2 : -2;
        for (int i = 0; i < buffer.length(); i++) {
            buffer.setCharAt(i, (char) (buffer.charAt(i) + mode));
        }
        return buffer.toString();
    }

    public void actionPerformed(ActionEvent e) {
        String message = e.getActionCommand();
        if (message.isEmpty()) return;
        ((JTextField) e.getSource()).setText("");

        if (message.startsWith("/")) {
            String[] args = message.split(" ");
            String keyword = args[0].substring(1);
            if (map.containsKey(keyword)) {
                Command command = map.get(keyword);
                if (!command.isSudo() || ChatClient.isSuperUser) {
                    command.execute(out, args);
                } else {
                    ChatPanel.addContent("Missing privileges", false);
                }
            } else {
                ChatPanel.addContent("Error processing", false);
            }
        } else {
            try {
                String[] separated = message.split(" ");
                for (int i = 0; i < separated.length; i++) {
                    for (Emote emote : emotes) {
                        if (separated[i].equals(emote.name())) {
                            separated[i] = "<img src=\"" + emote.getDestination() + "\">";
                        }
                    }
                }
                message = String.join(" ", separated);
                for (WebColor color : colors) {
                    if (message.startsWith(color.toString() + ":")) {
                        String content = message.split(":", 2)[1];
                        message = "<font color=\"" + color.getRGBString() + "\">" + content + "</font>";
                    }
                }
                out.writeByte(ChatClient.NORMAL_MESSAGE);
                out.writeUTF(protect(message, true));
                out.flush();
                ChatPanel.addMessage(message, chatPanel.getUsername());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
