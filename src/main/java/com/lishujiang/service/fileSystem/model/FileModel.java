package com.lishujiang.service.fileSystem.model;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: lishujiang
 * @date: 2021/04/22 10:24
 **/
@Data
public class FileModel {

    /**
     * 文件名或目录名
     */
    private String fileName;

    /**
     * 文件类型:用来识别是文件还是目录
     */
    private String type;

    /**
     * 当前节点在FAT磁盘块中的起始位置
     */
    private Integer diskPosition;

    /**
     * 文件的大小
     */
    private int size;

    /**
     * 位置
     */
    private String location;
    private int diskNum;

    /**
     * 创建时间
     */
    private Date createTime;
    private int length;
    private String content;

    /**
     * 属性
     */
    private int property;
    private boolean isReadOnly;

    public FileModel(String fileName) {
        super();
        this.fileName = fileName;
    }


    public FileModel(String fileName, String location, int diskNum) {
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
}
