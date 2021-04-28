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

    private String fileName; //文件名或目录名
    private String type; //文件类型:用来识别是文件还是目录
    /**
     * 当前节点在FAT磁盘块中的起始位置
     */
    private Integer diskPosition;
    private int size;    //文件的大小
    private String location; //位置
    private int diskNum;
    private Date createTime; //创建时间
    private int length;//1个字节
    private String content;//内容

    //文件的目录项 占用8个字节
    private int property;//1个字节  属性
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
