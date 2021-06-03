/*
 * This file is part of MOS
 * <p>
 * Copyright (c) 2021 by cooder.org
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos;

import org.cooder.mos.device.IDisk;
import org.cooder.mos.device.Impl.IDiskImpl;
import org.cooder.mos.shell.Shell;

public class App {
    public static void main(String[] args) throws Exception {
        // TODO new your implementation;
        IDisk disk = new IDiskImpl();
        MosSystem.fileSystem().bootstrap(disk);

        try {
            Shell shell = new Shell();
            shell.loop();
        } finally {
            MosSystem.fileSystem().shutdown();
        }
    }
}
