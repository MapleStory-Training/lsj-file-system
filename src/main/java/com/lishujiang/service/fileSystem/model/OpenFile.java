package com.lishujiang.service.fileSystem.model;

public class OpenFile {

	/**
	 * 0 以读打开  1以写打开
	 */
	private int flag;
	private File file;
	
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
}


