package org.dinhware.special;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.dinhware.ChatClient.KICK_USER;

public class KickCommand implements Command {

    @Override
    public void execute(DataOutputStream out, String... args) {
        try {
            out.writeByte(KICK_USER);
            out.writeUTF(Arrays.stream(args).skip(1).collect(Collectors.joining(" ")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSudo() {
        return true;
    }
}
