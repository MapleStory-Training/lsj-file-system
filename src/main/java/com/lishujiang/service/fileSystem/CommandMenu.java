package com.lishujiang.service.fileSystem;

import com.alibaba.fastjson.JSON;
import com.lishujiang.service.fileSystem.model.FdtNode;
import com.lishujiang.service.fileSystem.model.File;
import com.lishujiang.service.fileSystem.model.Folder;
import com.lishujiang.service.fileSystem.test.RAFTestFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 命令菜单
 * @author: lishujiang
 * @date: 2021/04/21 16:01
 **/
public class CommandMenu {
    private FdtNode point;
    private FdtNode rootFdt;
    private FATService fatService;

    private void initService() {
        RAFTestFactory.diskInit();
        fatService = new FATService();
        fatService.initFAT();
        Folder rootFolder = new Folder("root", "root", 255);
        String fdts = RAFTestFactory.read(4096, 3000);
        if (!StringUtils.isEmpty(fdts)) {
            rootFdt = FdtNode.transFileNodeTree(JSON.parseObject(fdts, FdtNode.class), new FdtNode(rootFolder, 1, 1));
        } else {
            rootFdt = new FdtNode(rootFolder, 1, 1);
        }
        point = rootFdt;
        meun();
    }

    public void meun() {
        Scanner s = new Scanner(System.in);
        System.out.println("***********" + "文件模拟系统初始化完成" + "***********");
        System.out.println("请输入命令（输入help查看命令表）：");
        String str = null;
        while ((str = s.nextLine()) != null) {
            if ("exit".equals(str)) {
                System.out.println("感谢您的使用！");
                break;
            }
            try {
                String[] strs = editStr(str);
                switch (strs[0]) {
                    case "echo":
                        FdtNode fileNodeTree = FdtNode.searchFileNodeTree(rootFdt, new FdtNode(), strs[1]);
                        fatService.save(strs[2], fileNodeTree.getDiskPosition());
                        break;
                    case "ll":
                        FdtNode.printOutNodeTree(point);
                        break;
                    case "cat":
                        if (strs.length < 2) {
                            System.out.println("您所输入的命令有误，请检查！");
                        } else {
                            if (FdtNode.findNodeType(rootFdt, strs[1]) == 1) {
                                System.out.println("文件夹无法打开查看，请查看文件");
                            } else {
                                if (FATService.getOpenFiles().getFiles().size() < FileSystemUtil.num) {
                                    FdtNode fdtNode = FdtNode.searchFileNodeTree(rootFdt, new FdtNode(), strs[1]);
                                    if (fdtNode == null || fdtNode.getNodeEntity() == null) {
                                        System.out.println("未找到要查看的文件");
                                        return;
                                    }
                                    FATService.catFile(fdtNode.getDiskPosition());
                                } else {
                                    System.out.println("已打开5个文件，无法再打开新文件。");
                                }
                            }
                        }
                        break;
                    case "create":
                        if (strs.length < 2) {
                            System.out.println("您所输入的命令有误，请检查！");
                        } else {
                            String path = ((Folder) point.getNodeEntity()).getFolderName();
                            int index1 = fatService.createFile(path, strs[1]);
                            if (index1 == FileSystemUtil.ERROR) {
                                System.out.println("抱歉，磁盘空间已满，无法创建新文件");
                            } else {
                                FdtNode currentNode = new FdtNode((File) FATService.getFAT(index1).getObject(), index1, 2);
                                point.addChildNode(currentNode);
                                RAFTestFactory.write(4096, JSON.toJSONString(rootFdt).getBytes());
                                System.out.println("文件" + strs[1] + "创建成功");
                            }
                        }
                        break;
                    case "mkdir":
                        if (strs.length < 2) {
                            System.out.println("您所输入的命令有误，请检查！");
                        } else {
                            String path = ((Folder) point.getNodeEntity()).getLocation() + "\\" + strs[1];
                            int index1 = fatService.createFolder(path, strs[1]);
                            if (index1 == FileSystemUtil.ERROR) {
                                System.out.println("抱歉，磁盘空间已满，无法创建新文件夹");
                            } else {
                                FdtNode currentNode = new FdtNode((Folder) FATService.getFAT(index1).getObject(), index1, 1);
                                point.addChildNode(currentNode);
                                RAFTestFactory.write(4096, JSON.toJSONString(rootFdt).getBytes());
                                System.out.println("文件夹" + strs[1] + "创建成功，当前文件路径：" + ((Folder) point.getNodeEntity()).getLocation());
                            }
                        }
                        break;
                    case "cd":
                        if (strs.length < 2) {
                            System.out.println("您所输入的命令有误，请检查！");
                        } else {
                            if (FdtNode.searchNodeTree(rootFdt, new FdtNode(), strs[1]) != null) {
                                point = FdtNode.searchNodeTree(rootFdt, new FdtNode(), strs[1]);
                                System.out.println("当前文件路径：" + ((Folder) point.getNodeEntity()).getLocation());
                            } else {
                                System.out.println("不存在此文件夹！");
                            }
                        }
                        break;
                    case "cd..":
                        if (point.getParentNode() == null) {
                            System.out.println("当前路径不存在上级目录");
                        } else {
                            point = point.getParentNode();
                            System.out.println("当前文件路径：" + ((Folder) point.getNodeEntity()).getLocation());
                        }
                        break;
                    case "help": {
                        System.out.println("命令如下：");
                        System.out.println("createFile FileName");
                        System.out.println("<创建文件 如：createFile marco>");
                        System.out.println();
                        System.out.println("mkdir Folder");
                        System.out.println("<创建目录 如：mkdir Folder >");
                        System.out.println();
                        System.out.println("cat FileName");
                        System.out.println("<打开文件 如：cat file >");
                        System.out.println();
                        System.out.println("cd FolderName");
                        System.out.println("<打开目录 如： cd myFolder >");
                        System.out.println();
                        System.out.println("cd..");
                        System.out.println("<返回上级目录 如： cd..");
                        System.out.println();
                        System.out.println("echo FileName content");
                        System.out.println("<写入数据 如： echo file xxx>");
                        System.out.println();
                        System.out.println("ll");
                        System.out.println("<查看当前目录文件 如：ll>");
                        System.out.println();
                        System.out.println("<退出 如：exit>");
                        break;
                    }
                    default:
                        for (String st : strs) {
                            System.out.println(st);
                        }
                        System.out.println("您所输入的命令有误，请检查！");
                }
                System.out.println("请输入命令（输入help查看命令表）：");
            } catch (Exception e) {
                System.out.println("您所输入的命令有误，请检查！");
            }
        }
    }

    public static String[] editStr(String str) {
        Pattern pattern = Pattern.compile("([a-zA-Z0-9.\\\\/]*) *");// 根据空格分割输入命令
        Matcher m = pattern.matcher(str);
        ArrayList<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(1));
        }
        String[] strs = list.toArray(new String[list.size()]);

        for (int i = 1; i < strs.length; i++) { // 判断除命令以外每一个参数中是否含有 "."
            int j = strs[i].indexOf(".");
            if (j != -1) { // 若含有"." 将其切割 取前部分作为文件名
                String[] index = strs[i].split("\\."); // 使用转义字符"\\."
                strs[i] = index[0];
            }
        }
        return strs;
    }


    public static void main(String[] args) {
        try {
            CommandMenu commandMenu = new CommandMenu();
            commandMenu.initService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
