package com.lishujiang.service.fileSystem.model;

public class FAT<T> {

    /**
     * 磁盘块存放的内容大小 255表示磁盘块已占用
     */
    private int index;

    /**
     * 该磁盘块中存放的是File还是Folder
     */
    private int type;

    /**
     * 文件占用磁盘块起始起始位置和FDT节点相关联（多个磁盘块的起始位置一样代表一个文件占用多个磁盘块）
     */
    private Integer diskPosition;

    /**
     * File或Folder
     */
    private T object;

    public FAT() {

    }

    public FAT(int index, int type, T object) {
        super();
        this.index = index;
        this.type = type;
        this.object = object;
    }

    public FAT(int index, int type, Integer diskPosition, T object) {
        super();
        this.index = index;
        this.type = type;
        this.object = object;
        this.diskPosition = diskPosition;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
