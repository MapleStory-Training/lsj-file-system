package com.lishujiang.service.fileSystem.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 目录结构
 * @author: lishujiang
 * @date: 2021/04/22 09:56
 **/
@Data
public class FdtNode<T> implements Serializable {
    /**
     * 当前节点的父节点
     */
    private FdtNode parentNode;
    /**
     * 当前节点
     */
    private T nodeEntity;
    /**
     * 当前节点在FAT磁盘块中的起始位置
     */
    private Integer diskPosition;
    /**
     * 节点类型
     */
    private Integer type;
    /**
     * 当前节点下的子节点，代表多个文件或者文件夹
     */
    private List<FdtNode> childNodes;

    private Integer length;

    public FdtNode(T nodeEntity, Integer diskPosition, Integer type) {
        this.nodeEntity = nodeEntity;
        this.diskPosition = diskPosition;
        this.type = type;
    }

    public FdtNode() {
    }

    public void addChildNode(FdtNode childNode) {
        childNode.setParentNode(this);
        if (this.childNodes == null) {
            this.childNodes = new ArrayList<FdtNode>();
        }
        this.childNodes.add(childNode);
    }

    public void removeChildNode(FdtNode childNode) {
        if (this.childNodes != null) {
            this.childNodes.remove(childNode);
        }
    }

    public FdtNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(FdtNode parentNode) {
        this.parentNode = parentNode;
    }

    public T getNodeEntity() {
        return nodeEntity;
    }

    public void setNodeEntity(T nodeEntity) {
        this.nodeEntity = nodeEntity;
    }

    public List<FdtNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<FdtNode> childNodes) {
        this.childNodes = childNodes;
    }

    public static <T> void printNodeTree(FdtNode<T> node) {
        for (FdtNode<T> childNode : node.getChildNodes()) {
            System.out.println(childNode.getNodeEntity().toString());
            if (childNode.getChildNodes() != null) {
                printNodeTree(childNode);
            }
        }
    }

    public static <T> FdtNode searchNodeTree(FdtNode<T> root, FdtNode current, String filename) {
        Folder rootFolder = (Folder) root.nodeEntity;
        if (rootFolder.getFolderName().equals(filename)) {
            return root;
        }
        if (CollectionUtils.isEmpty(root.getChildNodes())) {
            return null;
        }
        FdtNode fdtNode;
        for (FdtNode childNode : root.getChildNodes()) {
            if (childNode.getType() != 1) {
                continue;
            }
            System.out.println(childNode.getNodeEntity().toString());
            Folder childFolder = (Folder) childNode.nodeEntity;
            if (childFolder.getFolderName().equals(filename)) {
                current = childNode;
            }
            if (childNode.getChildNodes() != null) {
                current = searchNodeTree(childNode, current, filename);
            }

        }
        return current;
    }

    public static <T> FdtNode searchFileNodeTree(FdtNode<T> root, FdtNode current, String filename) {
        if (CollectionUtils.isEmpty(root.getChildNodes())) {
            return null;
        }
        FdtNode fdtNode;
        for (FdtNode childNode : root.getChildNodes()) {
            if (childNode.getType() == 1) {
                if (childNode.getChildNodes() != null) {
                    current = searchFileNodeTree(childNode, current, filename);
                    if (current != null && current.getNodeEntity() != null) {
                        break;
                    }
                }
                continue;
            }
            File childFile = (File) childNode.nodeEntity;
            if (filename.equals(childFile.getFileName())) {
                current = childNode;
            }
        }
        return current;
    }

    public static <T> FdtNode transFileNodeTree(FdtNode<T> node, FdtNode rootFdt) {

        if (node.getType() == 1) {
            node = (FdtNode<T>) JSON.parseObject(JSON.toJSONString(node), new TypeReference<FdtNode<Folder>>() {
            });
        } else {
            node = (FdtNode<T>) JSON.parseObject(JSON.toJSONString(node), new TypeReference<FdtNode<File>>() {
            });
        }
        if (CollectionUtils.isEmpty(node.getChildNodes())) {
            return node;
        }
        for (FdtNode childNode : node.getChildNodes()) {
            FdtNode newChildNode = null;
            if (childNode.getType() == 1) {
                newChildNode = JSON.parseObject(JSON.toJSONString(childNode), new TypeReference<FdtNode<Folder>>() {
                });
                newChildNode.setChildNodes(null);
                newChildNode.setParentNode(null);
                rootFdt.addChildNode(newChildNode);
            }
            if (childNode.getType() == 2) {
                newChildNode = childNode = JSON.parseObject(JSON.toJSONString(childNode), new TypeReference<FdtNode<File>>() {
                });
                newChildNode.setChildNodes(null);
                newChildNode.setParentNode(null);
                rootFdt.addChildNode(newChildNode);
            }
            if (childNode.getChildNodes() != null) {
                transFileNodeTree(childNode, newChildNode);
            }
        }
        return rootFdt;
    }


    public static <T> Integer findNodeType(FdtNode<T> root, String filename) {
        if (CollectionUtils.isEmpty(root.getChildNodes())) {
            return -1;
        }
        FdtNode fdtNode;
        Integer type = 0;
        for (FdtNode childNode : root.getChildNodes()) {
            if (childNode.getType() == 1) {
                Folder folder = (Folder) childNode.getNodeEntity();
                if (folder.getFolderName().equals(filename)) {
                    return childNode.getType();
                }
            } else {
                File file = (File) childNode.getNodeEntity();
                if (filename.equals(file.getFileName())) {
                    return childNode.getType();
                }
            }
            if (childNode.getChildNodes() != null) {
                type = findNodeType(childNode, filename);
            }
        }
        return type;
    }

    public static <T> void printOutNodeTree(FdtNode<T> node) {
        if (node.getType() != 1) {
            System.out.println("当前节点不是文件夹");
            return;
        }
        Folder rootFolder = (Folder) node.nodeEntity;
        if (CollectionUtils.isEmpty(node.getChildNodes())) {
            System.out.println("当前位置：" + rootFolder.getLocation() + ",当前文件夹下无内容");
            return;
        }
        for (FdtNode childNode : node.getChildNodes()) {
            if (childNode.getType() == 1) {
                Folder childFolder = (Folder) childNode.nodeEntity;
                System.out.println("当前位置：" + rootFolder.getLocation() + "包含：文件夹：" + childFolder.getFolderName());
            } else {
                File file = (File) childNode.nodeEntity;
                System.out.println("当前位置：" + rootFolder.getLocation() + "包含：文件" + file.getFileName());
            }
        }
    }
}
