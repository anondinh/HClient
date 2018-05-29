package org.dinhware.component;

import javax.swing.*;
import java.awt.*;

class ChatScroller extends JScrollPane {

    ChatScroller(Component component) {
        super(component);
        setPreferredSize(new Dimension(20, 150));
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
