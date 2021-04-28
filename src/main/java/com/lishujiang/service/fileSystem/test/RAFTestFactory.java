package com.lishujiang.service.fileSystem.test;

import java.io.*;

/**
 * @description:
 * @author: lishujiang
 * @date: 2021/04/27 09:59
 **/
public class RAFTestFactory {
    private static final String url = "wenjian.txt";
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


    public static class RAFTestMain {
        public static void main(String[] args) throws IOException {

//            RandomAccessFile raf = RAFTestFactory.getRAFWithModelR();
            String fdts = RAFTestFactory.read(4096,4222);
            System.out.println(fdts);
//            System.out.println("raf.length()->获取文本内容长度:" + raf.length());
//            System.out.println("raf.getFilePointer()->获取文本头指针:" + raf.getFilePointer());
//            raf.seek(4);
//            System.out.println("raf.getFilePointer()->第二次获取文本头指针:" + raf.getFilePointer());
        }

    }
    public static class RAFTestMain2 {
        public static void main(String[] args) throws IOException {
            RandomAccessFile raf = RAFTestFactory.getRAFWithModelRW();
            raf.seek(12);
            String word = "lsj";
            raf.write(word.getBytes());
        }
    }


    public static class RAFTestMain3 {
        public static void main(String[] args) throws IOException {
            RandomAccessFile raf = RAFTestFactory.getRAFWithModelRW();
            String content;
            while ((content = raf.readLine()) != null) {
                System.out.println(new String(content.getBytes("ISO-8859-1"), "utf-8"));
            }
        }
    }

    /**
     * 测试利用多线程进行文件的写操作
     */
    public static class Test {

        public static void main(String[] args) throws Exception {
            // 预分配文件所占的磁盘空间，磁盘中会创建一个指定大小的文件
            RandomAccessFile raf = RAFTestFactory.getRAFWithModelRW();
            raf.setLength(8); // 预分配 1M 的文件空间
            raf.close();

            // 所要写入的文件内容
            String s1 = "第一个字符串----";
            String s2 = "第二个字符串";
            String s3 = "第三个字符串";
            String s4 = "第四个字符串";
            String s5 = "第五个字符串";

            // 利用多线程同时写入一个文件
            new FileWriteThread(0*1,s1.getBytes()).start(); // 从文件的1024字节之后开始写入数据
            new FileWriteThread(6,s2.getBytes()).start(); // 从文件的2048字节之后开始写入数据
            new FileWriteThread(1024*3,s3.getBytes()).start(); // 从文件的3072字节之后开始写入数据
            new FileWriteThread(1024*4,s4.getBytes()).start(); // 从文件的4096字节之后开始写入数据
            new FileWriteThread(1024*5,s5.getBytes()).start(); // 从文件的5120字节之后开始写入数据
        }
    }

    /**
     * 利用线程在文件的指定位置写入指定数据
     */
    public static class FileWriteThread extends Thread{
        private int skip;
        private byte[] content;

        public FileWriteThread(int skip,byte[] content){
            this.skip = skip;
            this.content = content;
        }

        @Override
        public void run(){
            RandomAccessFile raf = null;
            try {
                raf = RAFTestFactory.getRAFWithModelRW();
                raf.seek(skip);
                raf.write(content);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static <T> void objectToDisk(T object) {

        try {

            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(object);
            new FileWriteThread(0, bo.toByteArray()).start();
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void diskInit() {
        try {
            long size = (long) (2000 * 1024 * 1024);
            RandomAccessFile r = RAFTestFactory.getRAFWithModelRW();
            r.setLength(size);
            r.close();
            System.out.println("磁盘文件初始化完成，文件名：fat.dat");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("磁盘文件初始化异常：" + e);
        }
    }

    public static void write(int skip,byte[] content){
        try {
            RandomAccessFile raf = new RandomAccessFile("wenjian.txt", "rw");
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
     * @param offset
     * @param len
     * @return
     */
    public static String read(int offset, int len) {
        byte[] bytes = new byte[len];
        try {
            RandomAccessFile raf = RAFTestFactory.getRAFWithModelRW();
            raf.seek(offset);
            raf.read(bytes);
            String str = "\\u0000";
            String rep = new String(bytes);
            rep = rep.replaceAll(str,"");
            return rep;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return null;
    }

    public static void main(String[] args) throws IOException {
//        RandomAccessFile raf = new RandomAccessFile("wenjian.txt", "r");
//        System.out.println(raf.getFilePointer());
//        System.out.println(raf.length());
//        System.out.println(read(0, (int) raf.length()));

//        Folder folder = new Folder("myFolder");
//        String ob = JSON.toJSONString(folder);
//        System.out.println(ob.getBytes().length);
//        new FileWriteThread(0,ob.getBytes()).start();
//        write(0,"asdfghjkl".getBytes());
//        write(4096,"rrrrrrrr".getBytes());
////        write(8092,"{\"childNodes\":[{\"diskPosition\":2,\"nodeEntity\":{\"content\":\"\",\"createTime\":\"2021年04月28日  09:46:19\",\"diskNum\":2,\"fileName\":\"file\",\"length\":0,\"location\":\"root\",\"property\":1,\"readOnly\":false,\"size\":\"8\",\"type\":\"File\"},\"parentNode\":{\"$ref\":\"$\"},\"type\":2}],\"diskPosition\":1,\"nodeEntity\":{\"createTime\":1619574374367,\"diskNum\":255,\"folderName\":\"root\",\"hasChild\":false,\"location\":\"root\",\"numOfFAT\":0,\"type\":\"Folder\"},\"type\":1}".getBytes());
        write(8092,"gggggggg".getBytes());
        String s = read(0,4096);
        String s2 = read(0,8092);
        String s3 = read(0,12000);
        String s4 = read(4096,3000);
    }

    public static class BigFileTester {

        public static void main(String[] args) throws FileNotFoundException, IOException {
            long cap = (long) (2000 * 1024 * 1024);

            long start2 = System.currentTimeMillis();
            RandomAccessFile r = new RandomAccessFile("fat.dat", "rw");
            r.setLength(cap);
            r.close();
            long duration2 = System.currentTimeMillis() - start2;
            System.out.println(duration2);
        }

    }
    public static class Offset {



        public static void main(String[] args) {

            String filePath = "wenjian.txt";

            // 创建字节输入流
            try {
                RandomAccessFile raf = new RandomAccessFile(filePath, "r");

                raf.seek(2); // 利用RandomAccessFile定位到第101个字节，之后再读文件
                byte[] bytes = new byte[5];
                raf.read(bytes);
                System.out.println(new String(bytes));

//                while (-1 != raf.read(bytes)) {
//                    System.out.println(new String(bytes));
//                }
//
//                FileInputStream fis = new FileInputStream(filePath);
//                // 创建装数据的数组
//                byte[] bytes = new byte[10];
//
//                // 调用read方法读取数据
//                fis.read(bytes, 2, 3);
//
//                // 打印读出来的数据
//                for (int i = 0; i < bytes.length; i++) {
//                    System.out.println(bytes[i]);
//
//                    System.out.println(new String(new byte[] {bytes[i]},"utf-8"));
//
//                }
//                System.out.println(new String(bytes, "utf-8"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * RandomAccessFile是属于随机读取类，是可以对文件本身的内容直接随机进行操作的，就是说可以指定位置
     * 的读取和写入内容
     * @author andy
     *
     */
    public static class RandomAccessFileTest {

        public static void main(String args[]) throws IOException {
//            write();
            read();
        }

        public static void write() throws IOException {
            //以读写的方式来访问该文件
            RandomAccessFile raf = new RandomAccessFile("random.txt", "rw");
            raf.writeBytes("Hello World!");
            raf.writeBoolean(true);
            raf.writeInt(30);
            raf.writeDouble(3.56);
            raf.close();
        }

        public static void read() throws IOException {
            RandomAccessFile raf = new RandomAccessFile("random.txt", "r");
            raf.seek(12);//设置指针的位置
            boolean booleanValue = raf.readBoolean();
            int intValue = raf.readInt();
            double doubleValue = raf.readDouble();
            raf.seek(0);//设置指针的位置为文件的开始部分
            byte[] bytes = new byte[12];
            for (int i=0; i<bytes.length; i++) {
                bytes[i] = raf.readByte();//每次读一个字节，并把它赋值给字节bytes[i]
            }
            String stringValue = new String(bytes);
            raf.skipBytes(1);//指针跳过一个字节
            int intValue2 = raf.readInt();
            raf.close();
            System.out.println(booleanValue);
            System.out.println(intValue);
            System.out.println(doubleValue);
            System.out.println(stringValue);
            System.out.println(intValue2);
        }



    }


}
