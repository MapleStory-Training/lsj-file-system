package com.lishujiang.service.fileSystem.model;

public class OpenFile {

	private int flag;// 0 以读打开  1以写打开
//	private Pointer read;
//	private Pointer write;
	private File file;
	
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
//	public Pointer getRead() {
//		return read;
//	}
//	public void setRead(Pointer read) {
//		this.read = read;
//	}
//	public Pointer getWrite() {
//		return write;
//	}
//	public void setWrite(Pointer write) {
//		this.write = write;
//	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}

	public static void main(String[] args) {
		String str = "NewFolder1";
		System.out.println(str.length());
		System.out.println(str.charAt(0));
	}
	
}


