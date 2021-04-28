package com.lishujiang.service.fileSystem.model;

public class Disk {

	private String diskName;
	
	
	public Disk(String diskName) {
		super();
		this.diskName = diskName;
	}
	
	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}
	
	@Override
	public String toString() {
		return diskName;
	}

}
