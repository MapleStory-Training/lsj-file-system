package com.lishujiang.service.fileSystem;

import com.alibaba.fastjson.JSON;
import com.lishujiang.service.fileSystem.model.FAT;
import com.lishujiang.service.fileSystem.model.FdtNode;
import com.lishujiang.service.fileSystem.model.File;
import com.lishujiang.service.fileSystem.model.Folder;
import com.lishujiang.service.fileSystem.test.RAFTestFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 命令菜单
 * @author: lishujiang
 * @date: 2021/04/21 16:01
 **/
public class CommandMenu {
    FdtNode point;

    private FATService fatService;
    private List<FAT> fatList;
    public Map<String, Folder> subMap = new HashMap<String, Folder>();
    public FdtNode rootFdt;

    private void initService() {
        fatList = new ArrayList<FAT>();
        fatService = new FATService();
        fatService.initFAT();
        Folder rootFolder = new Folder("root", "root", 255);
        subMap.put("root", rootFolder);
        String fdts = RAFTestFactory.read(4096, 3000);
        if (!StringUtils.isEmpty(fdts)) {
            rootFdt = FdtNode.transFileNodeTree(JSON.parseObject(fdts, FdtNode.class), new FdtNode(rootFolder, 1, 1));
        } else {
            rootFdt = new FdtNode(rootFolder, 1, 1);
        }
        point = rootFdt;
    }

    public void meun() {
        Scanner s = new Scanner(System.in);
        String str = null;
        System.out.println("***********" + "文件模拟系统初始化完成" + "***********");
        System.out.println();

        System.out.println("请输入命令（输入help查看命令表）：");
        while ((str = s.nextLine()) != null) {
            if (str.equals("exit")) {
                System.out.println("感谢您的使用！");
                break;
            }

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
                    System.out.println("命令如下（空格不能省略）：");
                    System.out
                            .println("createFile FileName fileType fileSize");
                    System.out.println("<创建文件 如：createFile marco txt 5 >");
                    System.out.println();
                    System.out
                            .println("createCatalog FatalogName");
                    System.out.println("<创建目录 如：createCatalog myFile >");
                    System.out.println();
                    System.out
                            .println("open Name.FileTypt");
                    System.out.println("<打开文件 如：open marco.txt >");
                    System.out.println();
                    System.out.println("cd CatalogName");
                    System.out.println("<打开目录 如： cd myFile >");
                    System.out.println();
                    System.out.println("cd..");
                    System.out.println("<返回上级目录 如： cd..");
                    System.out.println();
                    System.out
                            .println("delete FileName/CatalogName");
                    System.out.println("<删除文件或目录（目录必须为空）如：delete marco >");
                    System.out.println();
                    System.out
                            .println("rename FileName/CatalogName NewName");
                    System.out.println("<重命名文件或目录 如： rename myfile mycomputer >");
                    System.out.println();
                    System.out
                            .println("search FileAbsolutedRoad/CatalogAbsolutedRoad");
                    System.out.println("<根据绝对路径寻找文件或者目录 如： search root/marco >");
                    System.out.println();
                    System.out.println("showFAT");
                    System.out.println("<查看FAT表 如： showFAT>");
                    System.out.println();
                    System.out.println();
                    System.out.println("下列命令需要打开文件后操作：");
                    System.out
                            .println("addContents FileName ContentSize");
                    System.out.println("<在文件内增加内容 如：ddContents marco 4 >");
                    System.out.println();
                    System.out
                            .println("changeType FileName newType");
                    System.out.println("<改变文件类型 如： changeType marco doc>");
                    System.out.println();
                    break;
                }
                default:
                    for (String st : strs) {
                        System.out.println(st);
                    }
                    System.out.println("您所输入的命令有误，请检查！");
            }
            System.out.println("请输入命令（输入help查看命令表）：");
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


    public static class testFileSystem {
        public static void main(String[] args) {
            try {
                RAFTestFactory.diskInit();
                CommandMenu commandMenu = new CommandMenu();
                commandMenu.initService();
                commandMenu.meun();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
