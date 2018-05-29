package org.dinhware.special;

import org.dinhware.ChatClient;
import org.dinhware.component.ChatPanel;

import java.io.DataOutputStream;
import java.io.IOException;

import static org.dinhware.ChatClient.SUDO_REQUEST;

public class SudoCommand implements Command {

    @Override
    public void execute(DataOutputStream out, String... args) {
        if (args.length != 2 || !args[1].equals("dinhware")) {
            ChatPanel.addContent("Sudo request denied", false);
        } else {
            try {
                out.writeByte(SUDO_REQUEST);
                out.writeUTF(ChatClient.username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isSudo() {
        return false;
    }
}
