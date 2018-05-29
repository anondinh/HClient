package org.dinhware.component;

import org.dinhware.action.Message;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class ChatPanel extends JPanel {

    private static JTextField chatBar;
    private static JTextPane chat;
    private static String username;
    private static ChatScroller scroller;

    public ChatPanel(DataOutputStream stream, String address, String username) {
        ChatPanel.username = username;

        setLayout(new BorderLayout());

        chatBar = new JTextField();
        chatBar.setAction(new Message(this, stream));
        add(chatBar, BorderLayout.SOUTH);

        scroller = new ChatScroller(chat = new ChatDocument());
        add(scroller, BorderLayout.CENTER);

        add(new JLabel("Connected to " + address + " as " + username), BorderLayout.NORTH);

    }

    public String getUsername() {
        return username;
    }

    public static void toggle() {
        chatBar.setEditable(!chatBar.isEditable());
    }

    public static void addMessage(String message, String sender) {
        String formattedMessage = String.format("<b>%s</b>%s<size=5>%s</font>\n", sender, (sender.equals(username) ? " >> " : " &lt;&lt; "), message);
        addContent(formattedMessage, true);
    }

    public static void addContent(String content, boolean timestamp) {
        long timeMS = System.currentTimeMillis();
        Date instant = new Date(timeMS);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(instant);
        boolean shouldScroll = false;
        try {
            HTMLDocument doc = (HTMLDocument) chat.getDocument();
            HTMLEditorKit kit = (HTMLEditorKit) chat.getEditorKit();
            JScrollBar vsb = scroller.getVerticalScrollBar();

            BoundedRangeModel model = vsb.getModel();
            if (model.getExtent() + model.getValue() == model.getMaximum()) {
                shouldScroll = true;
            }
            kit.insertHTML(doc, doc.getLength(), timestamp ? time + ": " + content : content, 0, 0, null);
            if (shouldScroll) {
                chat.setCaretPosition(doc.getLength());
            }
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
    }
}
