package com.lishujiang.service.fileSystem.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Folder implements Serializable {
    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 磁盘位置
     */
    private int diskNum;

    /**
     * 类型
     */
    private String type;

    /**
     * 位置
     */
    private String location;

    /**
     * 位置
     */
    private String preLocation;

    /**
     * 位置
     */
    private String aftLocation;

    /**
     * 创建时间
     */
    private Date createTime;

    public Folder() {
    }

    public Folder(String folderName) {
        super();
        this.folderName = folderName;
    }

    public Folder(String folderName, String preLocation, String aftLocation) {
        super();
        this.folderName = folderName;
        this.preLocation = preLocation;
        this.aftLocation = aftLocation;
        this.createTime = new Date();
        this.type = "Folder";
    }

    public Folder(String folderName, String location, int diskNum) {
        super();
        this.folderName = folderName;
        this.location = location;
        this.createTime = new Date();
        this.diskNum = diskNum;
        this.type = "Folder";
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }


    public int getDiskNum() {
        return diskNum;
    }

    public void setDiskNum(int diskNum) {
        this.diskNum = diskNum;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return folderName;
    }
}
