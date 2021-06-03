/*
 * This file is part of MOS
 * <p>
 * Copyright (c) 2021 by cooder.org
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos.shell;

import org.cooder.mos.MosSystem;
import org.cooder.mos.Utils;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class Shell {
    public InputStream in = MosSystem.in;
    public PrintStream out = MosSystem.out;
    public PrintStream err = MosSystem.err;

    public void loop() throws Exception {
        Scanner scanner = null;
        try {
            scanner = new Scanner(in);
            while (true) {
                prompt();
                String line = scanner.nextLine();
                String cmd = line.trim();
                if ("exit".equals(cmd)) {
                    out.println("bye~");
                    break;
                } else if (cmd.length() == 0) {
                    continue;
                }
                execute(cmd);
            }
        } catch (Exception e) {
            err.println(e.getMessage());
        } finally {
            Utils.close(scanner);
        }
    }

    protected void execute(final String cmd) {
        new CommandLine(new Command.RootCommand()).execute(Utils.parseArgs(cmd));
    }

    protected void prompt() {
        MosSystem.fileSystem().prompt();
    }
}
