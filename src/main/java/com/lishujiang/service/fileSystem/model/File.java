package com.lishujiang.service.fileSystem.model;

import java.util.Date;

public class File {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 属性
     */
    private int property;

    /**
     * 起始盘块号
     */
    private int diskNum;

    /**
     * 长度
     */
    private int length;

    /**
     * 内容
     */
    private String content;

    /**
     * 位置
     */
    private String location;

    /**
     * 大小
     */
    private int size;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否只读
     */
    private boolean isReadOnly;

    public File() {
    }

    public File(String fileName) {
        super();
        this.fileName = fileName;
    }


    public File(String fileName, String location, int diskNum) {
        super();
        this.fileName = fileName;
        this.location = location;
        this.size = 8;
        this.createTime = new Date();
        this.diskNum = diskNum;
        this.type = "File";
        this.isReadOnly = false;
        this.length = 0;
        this.content = "";
        this.property = 1;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getProperty() {
        return property;
    }

    public void setProperty(int property) {
        this.property = property;
    }

    public int getDiskNum() {
        return diskNum;
    }

    public void setDiskNum(int diskNum) {
        this.diskNum = diskNum;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSize() {
        return Integer.toString(size);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }


    @Override
    public String toString() {
        return fileName;
    }
}
