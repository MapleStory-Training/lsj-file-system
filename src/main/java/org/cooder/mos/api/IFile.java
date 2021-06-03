/*
 * This file is part of MOS
 * <p>
 * Copyright (c) 2021 by cooder.org
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos.api;

public interface IFile {
    boolean exist();

    boolean isDir();

    boolean mkdir(String folderName);

    boolean createFile(String fileName);

    boolean delete();

    IFile[] listFiles();

    int length();

    long lastModified();

    void cd(String name);

    void ls();

    void echo(String content, String fileName);

    void cat(String fileName, String targetFilename);

    void format();
}
