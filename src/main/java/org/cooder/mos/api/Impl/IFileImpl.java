package org.cooder.mos.api.Impl;

import org.apache.commons.lang3.StringUtils;
import org.cooder.mos.api.IFile;
import org.cooder.mos.common.utils.FileSystemUtil;
import org.cooder.mos.fs.IFileSystem;
import org.cooder.mos.fs.Impl.IFileSystemImpl;

/**
 * @description: OS层，给应用提供了逻辑文件操作服务
 * @author: lishujiang
 * @date: 2021/05/22 17:25
 **/
public class IFileImpl implements IFile {

    IFileSystem iFileSystem = new IFileSystemImpl();
    private String path = null;

    @Override
    public boolean exist() {
        return false;
    }

    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public boolean mkdir(String folderName) {
        if (StringUtils.isEmpty(folderName)) {
            return false;
        }
        iFileSystem.mkdir(FileSystemUtil.ROOT_PATH + folderName);
        return true;
    }


    @Override
    public boolean createFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        iFileSystem.createFile(fileName);
        return true;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public IFile[] listFiles() {
        return new IFile[0];
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public void cd(String name) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        iFileSystem.cd(name);
    }

    @Override
    public void ls() {
        iFileSystem.ls();
    }

    @Override
    public void echo(String content, String fileName) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(fileName)) {
            return;
        }
        iFileSystem.echo(content, fileName);
    }

    @Override
    public void cat(String fileName, String targetFilename) {
        if (StringUtils.isEmpty(fileName) && StringUtils.isEmpty(targetFilename)) {
            return;
        }
        iFileSystem.cat(fileName, targetFilename);
    }

    @Override
    public void format() {
        iFileSystem.format();
    }
}
