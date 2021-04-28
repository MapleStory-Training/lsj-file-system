package com.lishujiang.service.fileSystem;

public class FileSystemUtil {

    public static int num = 5;
    public static String folderPath = "/images/folder.jpg";
    public static String folder1Path = "/images/folder1.jpg";
    public static String filePath = "/images/file.jpg";
    public static String file1Path = "/images/file1.jpg";
    public static String diskPath = "/images/disk.jpg";
    public static String imgPath = "/images/img1.jpg";

    public static int END = 255;
    public static int DISK = 0;
    public static int FOLDER = 1;
    public static int FILE = 2;

    public static int ERROR = -1;

    public static int flagRead = 0;
    public static int flagWrite = 1;

    /*
     * 动态地根据JLabel来设置JPanel的height
     */
    public static int getHeight(int n) {
        int a = 0;
        a = n / 4;
        if (n % 4 > 0) {
            a++;
        }
        return a * 120;
    }

    /**
     * 每一次保存时都算出
     *
     * @return
     */
    public static int getNumOfFAT(int length) {
        if (length <= 64) {
            return 1;
        } else {
            int n = 0;
            if (length % 64 == 0) {
                n = length / 64;
            } else {
                n = length / 64;
                n++;
            }
            return n;
        }
    }
}
