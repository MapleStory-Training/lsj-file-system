package org.cooder.mos.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @description: 记录文件或目录的相关属性
 * @author: lishujiang
 * @date: 2021/04/18 10:47
 **/

@Data
public class FileModel implements Serializable {

    /**
     * 文件名或目录名
     */
    private String fileName;
    /**
     * 文件属性:2：文件 1：文件夹
     */
    private int attribute;
    /**
     * 指向该文件/文件夹起始 cluster
     */
    private int startCluster;
    /**
     * 文件的大小
     */
    private int size;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 该文件或目录的上级目录
     */
    private FileModel father = null;
    /**
     * 文件子项:不会存入磁盘，需要的时候挂载
     */
    public transient Map<String, FileModel> subFiles;

    /**
     * 当前文件的路径
     */
    public transient String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileModel(){}

    /**
     * 创建文件
     * @param name
     * @param type
     * @param startNum
     * @param size
     */
    public FileModel(String name, int type, int startNum, int size){
        this.fileName = name;
        this.attribute = type;
        this.startCluster = startNum;
        this.size = size;
        this.createTime = new Date();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public int getStartCluster() {
        return startCluster;
    }

    public void setStartCluster(int startCluster) {
        this.startCluster = startCluster;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public FileModel getFather() {
        return father;
    }

    public void setFather(FileModel father) {
        this.father = father;
    }

    public Map<String, FileModel> getSubFiles() {
        return subFiles;
    }

    public void setSubFiles(Map<String, FileModel> subFiles) {
        this.subFiles = subFiles;
    }
}
