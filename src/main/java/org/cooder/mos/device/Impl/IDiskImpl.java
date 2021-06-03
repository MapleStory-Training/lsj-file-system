package org.cooder.mos.device.Impl;

import org.cooder.mos.common.utils.FileSystemUtil;
import org.cooder.mos.device.IDisk;
import org.cooder.mos.fs.fat16.Layout;

import static org.cooder.mos.fs.fat16.Layout.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @description:
 * @author: lishujiang
 * @date: 2021/05/23 13:54
 **/
public class IDiskImpl implements IDisk {
    @Override
    public int sectorSize() {
        return new BootSector().sectorSize;
    }

    @Override
    public int sectorCount() {
        return new BootSector().largeNumberOfSectors;
    }

    @Override
    public long capacity() {
        return new BootSector().largeNumberOfSectors * new BootSector().sectorSize;
    }

    @Override
    public byte[] readSector(int sectorIdx) {
        byte[] bytes = new byte[PER_SECTOR_SIZE];
        try {
            RandomAccessFile raf = getRAFWithModelRW();
            raf.seek(sectorIdx * PER_SECTOR_SIZE);
            raf.read(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public byte[] readSector(int sectorIdx, byte[] buffer) {
        try {
            RandomAccessFile raf = getRAFWithModelRW();
            raf.seek(sectorIdx * PER_SECTOR_SIZE);
            raf.read(buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void writeSector(int sectorIdx, byte[] sectorData) {
        try {
            RandomAccessFile raf = getRAFWithModelRW();
            raf.seek(sectorIdx * 512);
            raf.write(sectorData);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear(){
        try {
            RandomAccessFile r = getRAFWithModelRW();
            r.setLength(0);
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("磁盘文件初始化异常：" + e);
        }
    }

    @Override
    public void diskInit() {
        try {
            long size = (long) (2000 * 1024 * 1024);
            RandomAccessFile r = getRAFWithModelRW();
            r.setLength(size);
            r.close();
            System.out.println("***********" + "磁盘文件初始化完成" + "***********");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("磁盘文件初始化异常：" + e);
        }
    }

    private static final String url = FileSystemUtil.DISK_NAME;
    private static final String[] model = {"r", "rw", "rws", "rwd"};

    public static RandomAccessFile getRAFWithModelR() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(new File(url), model[0]);
        return raf;
    }

    public static RandomAccessFile getRAFWithModelRW() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(new File(url), model[1]);
        return raf;
    }

    public static RandomAccessFile getRAFWithModelRWS() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(new File(url), model[2]);
        return raf;
    }

    public static RandomAccessFile getRAFWithModelRWD() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(new File(url), model[3]);
        return raf;
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * 利用线程在文件的指定位置写入指定数据
     */
    public static class FileWriteThread extends Thread {
        private int skip;
        private byte[] content;

        public FileWriteThread(int skip, byte[] content) {
            this.skip = skip;
            this.content = content;
        }

        @Override
        public void run() {
            RandomAccessFile raf = null;
            try {
                raf = getRAFWithModelRW();
                raf.seek(skip);
                raf.write(content);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (Exception e) {
                }
            }
        }
    }


    public static void write(int skip, byte[] content) {
        try {
            RandomAccessFile raf = getRAFWithModelRW();
            raf.seek(skip);
            raf.write(content);
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 利用RandomAccessFile定位到指定偏移量后再读文件
     *
     * @param offset
     * @param len
     * @return
     */
    public static String read(int offset, int len) {
        byte[] bytes = new byte[len];
        try {
            RandomAccessFile raf = getRAFWithModelRW();
            raf.seek(offset);
            raf.read(bytes);
            String str = "\\u0000";
            String rep = new String(bytes);
            rep = rep.replaceAll(str, "");
            System.out.println("当前光标位置：" + raf.getFilePointer());
            return rep;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
