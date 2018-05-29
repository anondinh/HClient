package org.dinhware.special;

import java.io.DataOutputStream;

public interface Command {

    void execute(DataOutputStream out, String... args);

    boolean isSudo();
}
