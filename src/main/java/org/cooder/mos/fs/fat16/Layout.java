/*
 * This file is part of MOS
 * <p>
 * Copyright (c) 2021 by cooder.org
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.cooder.mos.fs.fat16;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Layout {
    public static final int RESERVED_SECTORS = 1;
    public static final int NUM_OF_FAT_COPY = 2;
    public static final int SECTORS_PER_FAT = 256;
    public static final int PER_SECTOR_SIZE = 512;
    public static final int PER_DIRECTOR_ENTRY_SIZE = 32;
    public static final int SECTORS_PER_CLUSTER = 64;
    public static final int PER_CLUSTER_SIZE = PER_SECTOR_SIZE * SECTORS_PER_CLUSTER;
    public static final int ROOT_ENTRIES_COUNT = (PER_CLUSTER_SIZE - PER_SECTOR_SIZE) / 32;

    public static final int VOLUME_START = 0;
    public static final int RESERVED_REGION_SIZE = RESERVED_SECTORS;

    public static final int FAT_REGION_START = VOLUME_START + RESERVED_REGION_SIZE;
    public static final int FAT_REGION_SIZE = NUM_OF_FAT_COPY * SECTORS_PER_FAT;

    public static final int ROOT_DIRECTORY_REGION_START = FAT_REGION_START + FAT_REGION_SIZE;
    // 根目录占用扇区数
    public static final int ROOT_DIRECTORY_REGION_SIZE = (ROOT_ENTRIES_COUNT * PER_DIRECTOR_ENTRY_SIZE) / PER_SECTOR_SIZE;

    public static final int DATA_REGION_START = ROOT_DIRECTORY_REGION_START + ROOT_DIRECTORY_REGION_SIZE;

    /**
     * 引导扇区Layout，涉及到整形数的都是大端字节序
     */
    public static class BootSector {
        // 跳转指令，3 bytes
        public final byte[] jmpCode = new byte[]{(byte) 0xEB, 0x3C, (byte) 0x90};

        // Oem Name 8 bytes
        public final byte[] oemName = new byte[]{'m', 'o', 's', '-', 'l', 's', 'j', 0};

        // 每扇区字节数，2 bytes
        public final short sectorSize = PER_SECTOR_SIZE;

        // 每簇扇区数，1 byte
        public final byte clusterWidth = SECTORS_PER_CLUSTER;

        // 保留扇区数，2bytes
        public final short reservedSectors = RESERVED_SECTORS;

        // FAT数量
        public final byte numOfFATCopy = NUM_OF_FAT_COPY;

        // 根目录项数
        public final short rootEntriesCount = ROOT_ENTRIES_COUNT;

        public final short smallNumberOfSectors = (short) 0xFFFF;

        public final byte mediaDescriptor = (byte) 0xFA;

        public final short sectorsPerFAT = SECTORS_PER_FAT;

        public final short sectorsPerTrack = 63;

        public final short numberOfHeads = 0;

        public final int hiddenSectors = 0;

        public final int largeNumberOfSectors = (short) 0xFFFF;

        public final byte driveNumber = 0;

        public final byte reserved = 0;

        public final byte extendedBootSignature = 0;

        public final int volumeSerialNumber = 0;

        public final byte[] volumeLabel = new byte[11];

        public final byte[] fileSystemType = new byte[]{'F', 'A', 'T', '1', '6', 'X', 0, 0};

        public final byte[] bootstrapCode = new byte[448];

        public final short bootSectorSignature = 0x55AA;

        public byte[] toBytes() {
            ByteBuffer buf = ByteBuffer.allocateDirect(512);
            buf.put(jmpCode);
            buf.put(oemName);
            buf.putShort(sectorSize);
            buf.put(clusterWidth);
            buf.putShort(reservedSectors);
            buf.put(numOfFATCopy);
            buf.putShort(rootEntriesCount);
            buf.putShort(smallNumberOfSectors);
            buf.put(mediaDescriptor);
            buf.putShort(sectorsPerFAT);
            buf.putShort(sectorsPerTrack);
            buf.putShort(numberOfHeads);
            buf.putInt(hiddenSectors);
            buf.putInt(largeNumberOfSectors);
            buf.put(driveNumber);
            buf.put(reserved);
            buf.put(extendedBootSignature);
            buf.putInt(volumeSerialNumber);
            buf.put(volumeLabel);
            buf.put(fileSystemType);
            buf.put(bootstrapCode);
            buf.putShort(bootSectorSignature);

            buf.rewind();

            byte[] data = new byte[512];
            buf.get(data);
            return data;
        }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(new Layout.BootSector().toBytes());
        System.out.println(new String(new Layout.BootSector().toBytes(),"utf-8"));
    }

    public static class DirectoryEntry {

        public static final int FILE_NAME_LENGTH = 8;

        public static final byte ATTR_MASK_READONLY = 0x01;
        public static final byte ATTR_MASK_HIDDEN = 0x02;
        public static final byte ATTR_MASK_SYSTEM = 0x04;
        public static final byte ATTR_MASK_VOLUME = 0x08;
        public static final byte ATTR_MASK_DIR = 0x10;
        public static final byte ATTR_MASK_ACHIEVE = 0x20;

        // 8 bytes
        public byte[] fileName = new byte[8];
        public byte[] extension = new byte[3];
        public byte attrs;
        public byte reserved;
        public byte creation;
        public int createTimeStamp;
        public short lastAccessDate;
        public short unused;
        public int lastWriteTimeStamp;
        public short startingCluster;
        public int fileSize;

        public byte[] toBytes() {
            ByteBuffer buf = ByteBuffer.allocateDirect(32);
            buf.put(fileName);
            buf.put(extension);
            buf.put(attrs);
            buf.put(reserved);
            buf.put(creation);
            buf.putInt(createTimeStamp);
            buf.putShort(lastAccessDate);
            buf.putInt(lastWriteTimeStamp);
            buf.putShort(startingCluster);
            buf.putInt(fileSize);

            buf.rewind();

            byte[] data = new byte[32];
            buf.get(data);
            return data;
        }

        public static DirectoryEntry from(byte[] data) {
            ByteBuffer buf = ByteBuffer.allocateDirect(32);
            buf.put(data);
            buf.rewind();

            DirectoryEntry e = new DirectoryEntry();
            buf.get(e.fileName, 0, 8);
            buf.get(e.extension, 0, 3);
            e.attrs = buf.get();
            e.reserved = buf.get();
            e.creation = buf.get();
            e.createTimeStamp = buf.getInt();
            e.lastAccessDate = buf.getShort();
            e.lastWriteTimeStamp = buf.getInt();
            e.startingCluster = buf.getShort();
            e.fileSize = buf.getInt();

            return e;
        }
    }

    public static int getClusterDataStartSector(int clusterIdx) {
        return clusterIdx * SECTORS_PER_CLUSTER;
    }

    public static int getSectorDataStartPos(int sectorIdx) {
        return sectorIdx * PER_SECTOR_SIZE;
    }

    public static int getClusterDataLastSector(int clusterIdx) {
        return getClusterDataStartSector(clusterIdx) + SECTORS_PER_CLUSTER - 1;
    }

//    public static void main(String args[]) {
////        File f = new File("demo.txt") ;        // 实例化File类的对象，给出路径
////        try{
////            f.createNewFile() ;        // 创建文件，根据给定的路径创建
////        }catch(IOException e){
////            e.printStackTrace() ;    // 输出异常信息
////        }
////        System.out.println("pathSeparator：" + File.pathSeparator) ;    // 调用静态常量
////        System.out.println("separator：" + File.separator) ;    // 调用静态常量
//        File f = new File("demo.txt");        // 实例化File类的对象
//        f.exists();
//    }
}
