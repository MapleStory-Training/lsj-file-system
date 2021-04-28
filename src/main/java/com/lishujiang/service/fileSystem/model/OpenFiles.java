package com.lishujiang.service.fileSystem.model;


import com.lishujiang.service.fileSystem.FileSystemUtil;

import java.util.ArrayList;
import java.util.List;

public class OpenFiles {

	private List<OpenFile> files;
	private int length;
	
	public OpenFiles(){
		files = new ArrayList<OpenFile>(FileSystemUtil.num);
		length = 0;
	}
	
	public void addFile(OpenFile openFile){
		files.add(openFile);
	}
	
	public void removeFile(OpenFile openFile){
		files.remove(openFile);
	}
	
	public List<OpenFile> getFiles() {
		return files;
	}

	public void setFiles(List<OpenFile> files) {
		this.files = files;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	
}
