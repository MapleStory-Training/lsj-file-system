package org.cooder.mos.fs.Impl;

import org.apache.commons.lang3.StringUtils;
import org.cooder.mos.MosSystem;
import org.cooder.mos.Utils;
import org.cooder.mos.common.model.FileModel;
import org.cooder.mos.common.utils.FileSystemUtil;
import org.cooder.mos.device.IDisk;
import org.cooder.mos.device.Impl.IDiskImpl;
import org.cooder.mos.fs.IFileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.cooder.mos.fs.fat16.Layout.*;
import static org.cooder.mos.fs.fat16.Layout.DirectoryEntry.ATTR_MASK_ACHIEVE;
import static org.cooder.mos.fs.fat16.Layout.DirectoryEntry.ATTR_MASK_DIR;

/**
 * @description:
 * @author: lishujiang
 * @date: 2021/05/23 17:42
 **/
public class IFileSystemImpl implements IFileSystem {

    private static short[] myFAT = new short[65536];
    private List<DirectoryEntry> directoryEntries = new LinkedList<>();
    private IDisk iDisk = new IDiskImpl();

    /**
     * 文件当前位置数据：其中key存储路径path
     */
    public static Map<String, List<DirectoryEntry>> directoryMap = new HashMap<>();

    public static DirectoryEntry currentEntry = null;

    @Override
    public void bootstrap(IDisk disk) throws IOException {
        // 初始化文件系统
        File homeFile = new File(FileSystemUtil.DISK_NAME);
        // 若存在则解析该文件生成文件系统
        if (homeFile.exists()) {
            // 1、myFat初始化
            int startSectorIndex = FAT_REGION_START;
            // 读取 SECTORS_PER_FAT 个扇区，也就是一整个FAT
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(SECTORS_PER_FAT * PER_SECTOR_SIZE);
            for (int i = 0; i < SECTORS_PER_FAT; i++) {
                byteBuffer.put(disk.readSector(startSectorIndex++));
            }
            byteBuffer.rewind();
            for (int i = 0; i < 65536; i++) {
                myFAT[i] = byteBuffer.getShort();
            }
            // 2、生成根目录
            getRootDirectory(disk);
        } else {
            format();
        }
    }

    private void getRootDirectory(IDisk disk) {
        ByteBuffer rootByteBuffer = ByteBuffer.allocateDirect(PER_CLUSTER_SIZE - PER_SECTOR_SIZE);
        for (int i = 513; i < 63 + 513; i++) {
            rootByteBuffer.put(disk.readSector(i));
        }
        rootByteBuffer.rewind();
        for (int i = 0; i < ROOT_ENTRIES_COUNT; i++) {
            byte[] data = new byte[32];
            rootByteBuffer.get(data);
            if (DirectoryEntry.from(data).startingCluster != 0) {
                directoryEntries.add(DirectoryEntry.from(data));
            }
        }
        // 3、初始化根文件:代表当前路径
        directoryMap.put(FileSystemUtil.ROOT_PATH, directoryEntries);
    }

    @Override
    public void shutdown() throws IOException {

    }

    @Override
    public void format() {
        iDisk.clear();
        iDisk.diskInit();

        // 1.写入根扇区
        int sectorIndex = VOLUME_START;
        byte[] bootSectorBytes = new BootSector().toBytes();
        iDisk.writeSector(sectorIndex++, bootSectorBytes);

        // 2.生成并写入FAT
        myFAT = new short[65536];
        myFAT[0] = (short) 0xFFF8;
        myFAT[1] = (short) 0xFFFF;
        // FAT表自身占用4个簇每个簇占用两个字节
        myFAT[2] = (short) 0xFFFF;
        myFAT[3] = (short) 0xFFFF;
        myFAT[4] = (short) 0xFFFF;
        myFAT[5] = (short) 0xFFFF;
        myFAT[6] = (short) 0xFFFF;
        myFAT[7] = (short) 0xFFFF;
        myFAT[8] = (short) 0xFFFF;
        sectorIndex = writeFatSectorIndex(sectorIndex);
        sectorIndex = writeFatSectorIndex(sectorIndex);

        // 3.写入根目录
        for (int i = 0; i < ROOT_DIRECTORY_REGION_SIZE; i++) {
            iDisk.writeSector(sectorIndex++, new byte[PER_SECTOR_SIZE]);
        }

        // 3、初始化根文件:代表当前路径
        directoryMap.put(FileSystemUtil.ROOT_PATH, new LinkedList<>());
        currentEntry = null;
    }

    private int writeFatSectorIndex(int sectorIndex) {
        // 65536 *2 /512 = 256 个扇区
        for (int j = 0; j < 256; j++) {
            // 一个fat项占用2个字节，256代表一个扇区(512byte)存储的FAT项为256个
            ByteBuffer buf = ByteBuffer.allocateDirect(512);
            for (int i = j * 256; i < (j + 1) * 256; i++) {
                buf.putShort(myFAT[i]);
            }
            buf.rewind();
            byte[] data = new byte[512];
            buf.get(data);
            iDisk.writeSector(sectorIndex++, data);
        }
        return sectorIndex;
    }

    @Override
    public void initFAT() {
        myFAT = new short[FileSystemUtil.TOTAL_COUNT];
        myFAT[0] = (short) 0xFFFF;
        myFAT[1] = (short) 0xFFFF;
    }

    @Override
    public FileModel searchRootDirect() {
        return null;
    }

    //得到myFAT中第一个为空的磁盘块索引
    @Override
    public int searchEmptyFromMyFAT() {
        for (int i = 2; i < myFAT.length; i++) {
            if (myFAT[i] == 0) {
                return i;
            }
        }
        return FileSystemUtil.ERROR;
    }

    @Override
    public void distributeFAT(Integer startCluster) {
        myFAT[startCluster] = (short) 0xFFFF;
    }

    @Override
    public boolean createFile(String fileName) {
        // 判断当前文件是否存在
        if (directoryEntries.size() > 0) {
            for (DirectoryEntry entry : directoryEntries) {
                if (entry.attrs == ATTR_MASK_ACHIEVE && new String(entry.fileName).equals(fileName)) {
                    System.out.println("创建的文件已存在");
                    return false;
                }
            }
        }
        // 判断是否是根目录：非根目录重新分配磁盘区域\
        String key = directoryMap.entrySet().iterator().next().getKey();
        if (FileSystemUtil.ROOT_PATH.equals(key)) {
            DirectoryEntry directoryEntry = createDirectory(fileName, ATTR_MASK_ACHIEVE);
            if (directoryEntry == null) {
                return false;
            }
            directoryEntries = directoryMap.get(key) != null ? directoryMap.get(key) : new LinkedList<>();
            directoryEntries.add(directoryEntry);
            setNumFAT(getEmptyOfFAT());
            // fat 起始扇区是1
            writeFatSectorIndex(1);
            // 根目录起始扇区是513
            writeRootDirectory(513, ROOT_DIRECTORY_REGION_SIZE);
        } else {
            // 1.获取当前文件记录的下一个簇的位置
            int cluster = currentEntry.startingCluster;
            // 2.文件夹的起始位置：一个簇占用32Kb 占用64个扇区
            int startRegion = cluster * 64;
            // 3.创建文件
            DirectoryEntry directoryEntry = createDirectory(fileName, ATTR_MASK_ACHIEVE);
            directoryEntries = directoryMap.get(key) != null ? directoryMap.get(key) : new LinkedList<>();
            directoryEntries.add(directoryEntry);
            // 4.分配簇
            setNumFAT(getEmptyOfFAT());
            writeFatSectorIndex(1);
            // 5.磁盘创建文件
            writeRootDirectory(startRegion, 64);
        }
        return true;
    }

    @Override
    public boolean mkdir(String folderName) {
        if (directoryMap.get(folderName) != null) {
            System.out.println("创建的文件夹已存在！");
            return false;
        }
        // 判断是否是根目录：非根目录重新分配磁盘区域\
        String key = directoryMap.entrySet().iterator().next().getKey();
        if (FileSystemUtil.ROOT_PATH.equals(key)) {
            DirectoryEntry directoryEntry = createDirectory(folderName, ATTR_MASK_DIR);
            if (directoryEntry == null) {
                return false;
            }
            directoryEntries = directoryMap.get(key) != null ? directoryMap.get(key) : new LinkedList<>();
            directoryEntries.add(directoryEntry);
            setNumFAT(getEmptyOfFAT());
            // fat 起始扇区是1
            writeFatSectorIndex(1);
            // 根目录起始扇区是513
            writeRootDirectory(513, ROOT_DIRECTORY_REGION_SIZE);
        } else {
            // 1.获取当前文件记录的下一个簇的位置
            int cluster = currentEntry.startingCluster;
            // 2.文件夹的起始位置：一个簇占用32Kb 占用64个扇区
            int startRegion = cluster * 64;
            // 3.创建文件
            DirectoryEntry directoryEntry = createDirectory(folderName, ATTR_MASK_DIR);
            directoryEntries = directoryMap.get(key) != null ? directoryMap.get(key) : new LinkedList<>();
            directoryEntries.add(directoryEntry);
            // 4.分配簇
            setNumFAT(getEmptyOfFAT());
            writeFatSectorIndex(1);
            // 5.磁盘创建文件
            writeRootDirectory(startRegion, 64);
        }
        return true;
    }

    public DirectoryEntry createDirectory(String folderName, byte type) {
        DirectoryEntry directoryEntry = new DirectoryEntry();
        String[] array = folderName.split("/");
        String name = array[array.length - 1];
        byte[] nameBytes = getNameBytes(name);
        if (nameBytes == null) {
            return null;
        }
        int sec = (int) (System.currentTimeMillis() / 1000);
        directoryEntry.fileName = nameBytes;
        directoryEntry.attrs = type;
        directoryEntry.fileSize = 0x00;
        directoryEntry.createTimeStamp = sec;
        directoryEntry.creation = 0x00;
        directoryEntry.lastAccessDate = (short) (sec >>> 16 & 0xFFFF);
        directoryEntry.lastWriteTimeStamp = (sec & 0xFFFF);
        directoryEntry.startingCluster = (short) getEmptyOfFAT();
        directoryEntry.unused = 0x0000;
        return directoryEntry;
    }

    private byte[] getNameBytes(String name) {
        byte[] nameBytes = new byte[8];
        for (int i = 0; i < name.getBytes().length; i++) {
            nameBytes[i] = name.getBytes()[i];
        }
        if (name.getBytes().length > 8) {
            System.out.println("文件名称过长！");
            return null;
        } else if (name.getBytes().length < 8) {
            for (int i = name.getBytes().length; i < 8; i++) {
                nameBytes[i] = 0X00;
            }
        }
        return nameBytes;
    }

    /**
     * 创建目录
     *
     * @param index        目录的起始扇区
     * @param sectorsCount 共占用的扇区数量
     */
    private void writeRootDirectory(int index, int sectorsCount) {
        int currentPoint = 0;
        // ROOT_DIRECTORY_REGION_SIZE = 63 根目录占用扇区数
        for (int i = 0; i < sectorsCount; i++) {
            ByteBuffer buf = ByteBuffer.allocateDirect(512);
            for (int j = 0; j < 16; j++) {
                if (directoryEntries.size() > currentPoint && directoryEntries.get(j) != null) {
                    buf.put(directoryEntries.get(j).toBytes());
                    currentPoint = ++currentPoint;
                } else {
                    buf.put(new byte[32]);
                }
            }
            buf.rewind();
            byte[] data = new byte[512];
            buf.get(data);
            iDisk.writeSector(index + i, data);
        }
    }

    private void writeContent(int startNum, int sectorsCount, String content) {
        ByteBuffer buf = ByteBuffer.allocateDirect(64 * sectorsCount * 512);
        buf.put(content.getBytes());
        int empty = 64 * sectorsCount * 512 - content.getBytes().length;
        if (empty > 0) {
            buf.put(new byte[empty]);
        }
        buf.rewind();
        // count 占用簇的数量 * 一个簇占用的扇区数
        for (int i = 0; i < 64 * sectorsCount; i++) {
            byte[] data = new byte[512];
            buf.get(data);
            iDisk.writeSector(startNum + i, data);
        }
    }

    public long getWriteTime(DirectoryEntry entry) {
        long sec = 0x0000 | entry.lastAccessDate;
        sec = sec << 16;
        sec = sec | (0xFFFF & entry.lastAccessDate);
        return sec * 1000;
    }

    @Override
    public void ls() {
        String key = directoryMap.entrySet().iterator().next().getKey();
        List<DirectoryEntry> directoryEntries = directoryMap.get(key);
        for (DirectoryEntry entry : directoryEntries) {
            String time = Utils.time2String(getWriteTime(entry));
            System.out.println((int) entry.startingCluster + "     " + time + "    " + new String(entry.fileName));
        }
    }

    @Override
    public void cd(String pathName) {
        // 获取当前文件的路径
        String key = directoryMap.entrySet().iterator().next().getKey();
        if (pathName.equals("..")) {
            String[] strArray = key.split("/");
            directoryMap.clear();
            directoryEntries = new LinkedList<>();
            getRootDirectory(iDisk);
            for (int i = 0; i < strArray.length - 1; i++) {
                if (StringUtils.isNotEmpty(strArray[i])) {
                    switchDirectory(strArray[i]);
                }
            }
            return;
        }
        // 获取当前文件的路径
        switchDirectory(pathName);
    }

    @Override
    public void echo(String content, String fileName) {
        // 查找文件对应的目录条目：获取簇信息
        String key = directoryMap.entrySet().iterator().next().getKey();
        for (DirectoryEntry entry : directoryMap.get(key)) {

            if (entry.attrs == ATTR_MASK_ACHIEVE && Arrays.equals(entry.fileName, getNameBytes(fileName))) {
                // 1.获取当前文件记录的下一个簇的位置
                int cluster = entry.startingCluster;
                // 2.文件夹的起始位置：一个簇占用32Kb 占用64个扇区
                int startRegion = cluster * 64;
                // 3.计算当前文件内容占用的簇的数量：一个簇32kb
                int sectorCount = content.getBytes().length / (32 * 1024) + 1;
                // 4.分配簇
                for (int i = 0; i < sectorCount; i++) {
                    int num = i + 1;
                    if (num == sectorCount) {
                        myFAT[cluster + i] = (short) 0xFFFF;
                    } else {
                        myFAT[cluster + i] = (short) (cluster + num);
                    }
                }
                // 5.写FAT
                writeFatSectorIndex(1);
                // 6.写数据
                writeContent(startRegion, sectorCount, content);
            }
        }
    }

    @Override
    public void cat(String fileName, String targetFilename) {
        if (StringUtils.isNotEmpty(targetFilename)) {
            // 1.从文件1/2取数据
            String data = readFile(fileName);
            String copyData = readFile(targetFilename);
            // 2.将文件1的数据赋值到文件2
            copyData = copyData + data;
            // 3.文件2将数据写入磁盘
            echo(copyData, targetFilename);
            return;
        }
        readFile(fileName);
    }

    @Override
    public void prompt() {
        String key = directoryMap.entrySet().iterator().next().getKey();
        MosSystem.out.println(String.format("lishujiang@mos-nil:%s$", key));
    }

    private String readFile(String fileName) {
        // 查找文件对应的目录条目：获取簇信息
        String key = directoryMap.entrySet().iterator().next().getKey();
        String str = null;
        for (DirectoryEntry entry : directoryMap.get(key)) {
            if (entry.attrs == ATTR_MASK_ACHIEVE && Arrays.equals(entry.fileName, getNameBytes(fileName))) {
                // 1.获取当前文件记录的下一个簇的位置
                int cluster = entry.startingCluster;
                // 2.文件夹的起始位置：一个簇占用32Kb 占用64个扇区
                int startRegion = cluster * 64;
                // 3.获取当前文件占用的簇的数量
                int clusterCount = 1;
                while (myFAT[cluster] != (short) 0xFFFF) {
                    cluster++;
                    clusterCount++;
                }
                // 4.磁盘读取文件
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(clusterCount * PER_CLUSTER_SIZE);
                for (int i = 0; i < 64 * clusterCount; i++) {
                    byteBuffer.put(iDisk.readSector(startRegion++));
                }
                byteBuffer.rewind();
                byte[] data = new byte[clusterCount * PER_CLUSTER_SIZE];
                byteBuffer.get(data);
                str = new String(data);
                System.out.println(str);
                String temp = "\\u0000";
                str = str.replaceAll(temp, "");
            }
        }
        return str;
    }

    private void switchDirectory(String pathName) {
        String key = directoryMap.entrySet().iterator().next().getKey();
        String currentPath = "";
        if (key.equals(FileSystemUtil.ROOT_PATH)) {
            currentPath = key + pathName;
        } else {
            currentPath = key + "/" + pathName;
        }

        List<DirectoryEntry> directoryEntries = directoryMap.get(key);
        for (int i = 0; i < directoryEntries.size(); i++) {
            byte[] bytes = directoryEntries.get(i).fileName;
            int length = 0;
            for (int j = 0; j < bytes.length; j++) {
                if (bytes[j] == 0) {
                    continue;
                }
                length++;
            }
            byte[] newByte = new byte[length];
            for (int j = 0; j < newByte.length; j++) {
                newByte[j] = bytes[j];
            }
            if (pathName.equals(new String(newByte))) {
                currentEntry = directoryEntries.get(i);
                break;
            }
        }
        if (currentEntry == null) {
            System.out.println("当前目录无此文件");
            return;
        }
        List<DirectoryEntry> list = new LinkedList<>();
        int cluster = currentEntry.startingCluster;
        // 一个簇占用32Kb 占用64个扇区
        int startRegion = cluster * 64;
        // 获取磁盘当前文件目录
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(PER_CLUSTER_SIZE);
        for (int i = 0; i < SECTORS_PER_CLUSTER; i++) {
            byteBuffer.put(iDisk.readSector(startRegion++));
        }
        byteBuffer.rewind();
        for (int i = 0; i < 64 * 512 / 32; i++) {
            byte[] data = new byte[32];
            byteBuffer.get(data);
            if (DirectoryEntry.from(data).startingCluster != 0) {
                list.add(DirectoryEntry.from(data));
            }
        }
        directoryMap.clear();
        directoryMap.put(currentPath, list);
    }

    //得到磁盘块的使用
    public int getNumOfFAT() {
        int n = 0;
        for (int i = 2; i < myFAT.length; i++) {
            if (myFAT[i] != 0x00) {
                n++;
            }
        }
        return n;
    }

    /**
     * 获取空的簇
     *
     * @return
     */
    public int getEmptyOfFAT() {
        for (int i = 2; i < myFAT.length; i++) {
            if (myFAT[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 簇占用
     *
     * @return
     */
    public void setNumFAT(int num) {
        myFAT[num] = (short) 0xFFFF;
    }

}
