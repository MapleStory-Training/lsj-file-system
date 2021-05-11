package com.lishujiang.service.fileSystem;

public class FileSystemUtil {

    public static int num = 5;
    public static int END = 255;
    public static int DISK = 0;
    public static int FOLDER = 1;
    public static int FILE = 2;

    public static int ERROR = -1;

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
