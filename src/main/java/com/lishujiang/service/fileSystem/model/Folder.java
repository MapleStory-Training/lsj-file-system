package com.lishujiang.service.fileSystem.model;

import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Folder implements Serializable {


	private String folderName;
	private int diskNum;
	private String type;
	
	private boolean hasChild;
	private int numOfFAT;
	
	//查看的属性
	private String location; //位置

	private String preLocation; //位置

	private String aftLocation; //位置

	private Date createTime; //创建时间

	public Folder() {
	}
	public Folder(String folderName) {
		super();
		this.folderName = folderName;
	}

	public Folder(String folderName, String preLocation,String aftLocation) {
		super();
		this.folderName = folderName;
		this.preLocation = preLocation;
		this.aftLocation = aftLocation;
		this.createTime = new Date();
		this.type = "Folder";
	}
	
	public Folder(String folderName, String location, int diskNum){
		super();
		this.folderName = folderName;
		this.location = location;
		this.createTime = new Date();
		this.diskNum = diskNum;
		this.type = "Folder";
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}


	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
	}

	public boolean isHasChild() {
		return hasChild;
	}

	public void setHasChild(boolean hasChild) {
		this.hasChild = hasChild;
	}

	public int getNumOfFAT() {
		return numOfFAT;
	}

	public void setNumOfFAT(int numOfFAT) {
		this.numOfFAT = numOfFAT;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	@Override
	public String toString() {
		return folderName;
	}
}
