/*
 * This file is part of Dkimi.
 * <p>
 * Copyright (c) 2016-2019 by yanxiyue
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos.fs;

import java.io.IOException;

import org.cooder.mos.common.model.FileModel;
import org.cooder.mos.device.IDisk;

public interface IFileSystem {

    /**
     * 目录路径分隔符
     */
    char separator = '/';

    /**
     * 引动启动文件系统
     * 
     * @param disk
     */
    void bootstrap(IDisk disk) throws IOException;

    /**
     * 关闭文件系统
     * 
     * @throws IOException
     */
    void shutdown() throws IOException;

    /**
     * 格式化
     * 
     * @throws IOException
     */
    void format();

    /**
     * 初始化
     */
    void initFAT();

    /**
     * 获取跟目录
     * @return
     */
    public FileModel searchRootDirect();

    /**
     * 获取空闲磁盘块
     * @return
     */
    int searchEmptyFromMyFAT();

    /**
     * 分配磁盘块
     * @return
     */
    void distributeFAT(Integer startCluster);
    
    // TODO add your code 文件操作
    /**
     * 创建文件
     * @return
     */
    boolean createFile(String name);

    /**
     * 创建文件夹
     * @param folderName
     * @return
     */
    boolean mkdir(String folderName);

    /**
     * 展示内容
     */
    void ls();

    /**
     * 切换目录
     * @param pathName
     */
    void cd(String pathName);

    /**
     * 输出
     * @param content
     * @param fileName
     */
    void echo(String content, String fileName);

    /**
     * cat
     * @param fileName
     * @param targetFilename
     */
    void cat(String fileName, String targetFilename);

    /**
     * 提示
     */
    void prompt();


}
