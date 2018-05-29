package org.dinhware;

import java.io.DataOutputStream;

public interface Callback {

    void setChat(DataOutputStream outputStream, String address, String username);

    void initializeListener(ListenerThread thread);
}
