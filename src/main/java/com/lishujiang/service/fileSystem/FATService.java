package com.lishujiang.service.fileSystem;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lishujiang.service.fileSystem.model.*;
import com.lishujiang.service.fileSystem.test.RAFTestFactory;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FATService {

	private static FAT[] myFAT;
	private static OpenFiles openFiles;
	private Integer length;


	public FATService(){
		openFiles = new OpenFiles();
	}

	public void addOpenFile(FAT fat, int flag){
		OpenFile openFile = new OpenFile();
		openFile.setFile((File)fat.getObject());
		openFile.setFlag(flag);
		openFiles.addFile(openFile);
	}

	public void removeOpenFile(FAT fat){
		for (int i=0; i<openFiles.getFiles().size(); i++){
			if (openFiles.getFiles().get(i).getFile() == (File)fat.getObject()){
				openFiles.getFiles().remove(i);
			}
		}
	}

	public boolean checkOpenFile(FAT fat){
		for (int i=0; i<openFiles.getFiles().size(); i++){
			if (openFiles.getFiles().get(i).getFile() == (File)fat.getObject()){
				return true;
			}
		}
		return false;
	}

	public void initFAT(){
		myFAT = new FAT[128];

		String fats = RAFTestFactory.read(0,4096);
		if (!StringUtils.isEmpty(fats)){
			List<FAT> objects = JSON.parseArray(fats, FAT.class);
			for (int i = 0; i <objects.size() ; i++) {
				if (objects.get(i) == null){
					break;
				}
				if (objects.get(i).getType() == 1){
					myFAT[i] = JSON.parseObject(JSON.toJSONString(objects.get(i)),new TypeReference<FAT<Folder>>(){});
				}else {
					myFAT[i] = JSON.parseObject(JSON.toJSONString(objects.get(i)),new TypeReference<FAT<File>>(){});
				}
//				myFAT[i] = (FAT) objects[i];
			}
		}else {
			myFAT[0] = new FAT(FileSystemUtil.END, FileSystemUtil.DISK, null);
			myFAT[1] = new FAT(FileSystemUtil.END, FileSystemUtil.DISK, new Disk("C"));
		}

	}
	
	//创建文件夹
	public int createFolder(String path, String name){
		String folderName = null;
		boolean canName = true;
		int index = 1;
		//得到新建文件夹名字
		do {
			if(name.equals("rdm"))
			{folderName = "NewFolder";
			folderName += index;
			}
			else {
				folderName=name;
			}
			canName = true;
			for (int i=0; i<myFAT.length; i++){
				if (myFAT[i] != null){
					if (myFAT[i].getType() == FileSystemUtil.FOLDER){
						Folder folder = (Folder)myFAT[i].getObject();
						if (path.equals(folder.getLocation())){
							if (folderName.equals(folder.getFolderName())){
								canName = false;
							}
						}
					}
				}
			}
			index ++;
		} while (!canName);
		//在myFAT中添加文件夹
		int index2 = searchEmptyFromMyFAT(); 
		if (index2 == FileSystemUtil.ERROR){
			return FileSystemUtil.ERROR;
		} else {
			Folder folder = new Folder(folderName, path, index2);
			myFAT[index2] = new FAT(FileSystemUtil.END, FileSystemUtil.FOLDER,index2, folder);
			System.out.println("myFAT:"+JSON.toJSONString(myFAT).getBytes().length);
			RAFTestFactory.write(0,JSON.toJSONString(myFAT).getBytes());
		}
		return index2;
	}
	
	/**
	 * 创建文件
	 * @return
	 */
	public int createFile(String path, String name){
		String fileName = null;
		boolean canName = true;
		int index = 1;
		//得到新建文件夹名字
		do {
			if(name.equals("rdm")){
			fileName = "NewFile";
			fileName += index;}
			else {
				fileName=name;
			}
			canName = true;
			
			for (int i=0; i<myFAT.length; i++){
				if (myFAT[i] != null){
					if (myFAT[i].getType() == FileSystemUtil.FILE){
						File file = (File)myFAT[i].getObject();

						if (path.equals(file.getLocation())){
							if (fileName.equals(file.getFileName())){
								canName = false;
							}
						}
					}
				}
			}
			index ++;
		} while (!canName);
		//在myFAT中添加文件夹
		int index2 = searchEmptyFromMyFAT(); 
		if (index2 == FileSystemUtil.ERROR){
			return FileSystemUtil.ERROR;
		} else {
			// 创建文件
			File file = new File(fileName, path, index2);
			myFAT[index2] = new FAT(FileSystemUtil.END, FileSystemUtil.FILE, index2, file);
			System.out.println("myFAT:"+JSON.toJSONString(myFAT).getBytes().length);
			RAFTestFactory.write(0,JSON.toJSONString(myFAT).getBytes());
		}
		return index2;
	}


	/**
	 * 保存文件内容到磁盘
	 * @return
	 */
	public int saveContentDisk(){
		return 0;
	}
	/**
	 * 保存文件或者文件夹到磁盘的目录区域
	 * @return
	 */
	public int saveFdtDisk(){
		return 0;
	}

	
	//得到myFAT中第一个为空的磁盘块索引
	public int searchEmptyFromMyFAT(){
		for (int i=2; i<myFAT.length; i++){
			if (myFAT[i] == null){
				return i;
			}
		}
		return FileSystemUtil.ERROR;
	}
	
	//得到磁盘块的使用
	public int getNumOfFAT(){
		int n = 0;
		for (int i=2; i<myFAT.length; i++){
			if (myFAT[i] != null){
				n++;
			}
		}
		return n;
	}
	
	//得到空的磁盘块数量
	public int getSpaceOfFAT(){
		int n = 0;
		for (int i=2; i<myFAT.length; i++){
			if (myFAT[i] == null){
				n++;
			}
		}
		return n;
	}

	/**
	 * 保存数据
	 */
	public void save(String str,Integer index){
		length = str.length();
		int num = FileSystemUtil.getNumOfFAT(length);
		File file = (File)myFAT[index].getObject();
		if (file.getContent().length() <= 0 && num <= 1){
			file.setLength(length);
			file.setContent(str);
			file.setSize(num);
			file.setDiskNum(index*4096);
			RAFTestFactory.write(index*4096,str.getBytes());
		} else if (num > 1) {
			boolean boo = saveToModifyFATS2(num, myFAT[index], str);
		} else {
			file.setLength(length);
			file.setContent(str);
		}
		file.setSize(num);

	}
	
	public boolean saveToModifyFATS2(int num, FAT fat ,String content){
		//从哪片磁盘开始
		int begin = ((File)fat.getObject()).getDiskNum();
		int index = myFAT[begin].getIndex();
		int oldNum = 1;
		while (index != FileSystemUtil.END){
			oldNum ++;
			if (myFAT[index].getIndex() == FileSystemUtil.END){
				begin = index;
			}
			index = myFAT[index].getIndex();
		}
		
		//
		if (num > oldNum){
			//需要添加磁盘块
			int n = num - oldNum;
			if (this.getSpaceOfFAT() < n){
				System.out.println("保存的内容已经超过磁盘的容量");
				return false;
			}
			int space = this.searchEmptyFromMyFAT();
			myFAT[begin].setIndex(space);
			File file = ((File)fat.getObject());
			for (int i=0; i<=n; i++){
				File newFile = new File(file.getFileName(),file.getLocation(),file.getDiskNum());
				space = this.searchEmptyFromMyFAT();
				int end = (i + 1) * 64;
				String str = content.substring(Math.min((i * 64),content.length()), Math.min(end, content.length()));
				if (i == n){

					newFile.setContent(str);
					RAFTestFactory.write(n*4096,str.getBytes());
					newFile.setDiskNum(n*4096);
					newFile.setLength(str.length());
					myFAT[space] = new FAT(255, FileSystemUtil.FILE, newFile);
				} else {
					newFile.setContent(str);
					newFile.setLength(str.length());
					newFile.setDiskNum(space*4096);
					RAFTestFactory.write(space*4096,str.getBytes());
					myFAT[space] = new FAT(20, FileSystemUtil.FILE, newFile);
					int space2 = this.searchEmptyFromMyFAT();
					myFAT[space].setIndex(space2);
				}
			}
			return true;
		} else {
			//不需要添加磁盘块
			return true;
		}
	}
	
	public List<Folder> getFolders(String path){
		List<Folder> list = new ArrayList<Folder>();
		for (int i=0; i<myFAT.length; i++){
			if (myFAT[i] != null){
				if (myFAT[i].getObject() instanceof Folder){
					if (((Folder)(myFAT[i].getObject())).getLocation().equals(path)){
						list.add((Folder)myFAT[i].getObject());
					}
				}
			}
		}
		return list;
	}
	
	public List<File> getFiles(String path){
		List<File> list = new ArrayList<File>();
		for (int i=0; i<myFAT.length; i++){
			if (myFAT[i] != null){
				if (myFAT[i].getObject() instanceof File){
					if (((File)(myFAT[i].getObject())).getLocation().equals(path)){
						list.add((File)myFAT[i].getObject());
					}
				}
			}
		}
		return list;
	}
	
	public List<FAT> getFATs(String path){
		List<FAT> fats = new ArrayList<FAT>();
		for (int i=0; i<myFAT.length; i++){
			if (myFAT[i] != null){
				if (myFAT[i].getObject() instanceof Folder){
					if (((Folder)(myFAT[i].getObject())).getLocation().equals(path) && myFAT[i].getIndex() == FileSystemUtil.END){
						fats.add(myFAT[i]);
					}
				}
			}
		}
		for (int i=0; i<myFAT.length; i++){
			if (myFAT[i] != null){
				if (myFAT[i].getObject() instanceof File){
					if (((File)(myFAT[i].getObject())).getLocation().equals(path) && myFAT[i].getIndex() == FileSystemUtil.END){
						fats.add(myFAT[i]);
					}
				}
			}
		}
		return fats;
		
	}
	
	public void modifyLocation(String oldPath, String newPath){
		for (int i=0; i<myFAT.length; i++){
			if (myFAT[i] != null){
				if (myFAT[i].getType()==FileSystemUtil.FILE){
					if (((File)myFAT[i].getObject()).getLocation().contains(oldPath)){
						((File)myFAT[i].getObject()).setLocation(((File)myFAT[i].getObject()).getLocation().replace(oldPath, newPath));
					}
				} else if (myFAT[i].getType()==FileSystemUtil.FOLDER){
					if (((Folder)myFAT[i].getObject()).getLocation().contains(oldPath)){
						((Folder)myFAT[i].getObject()).setLocation(((Folder)myFAT[i].getObject()).getLocation().replace(oldPath, newPath));
					}
				}
			}
		}
				
	}
	
	public int delete(JPanel jp1, FAT fat, Map<String, DefaultMutableTreeNode> map) {
		if (fat.getType() == FileSystemUtil.FILE){
			//---------------->文件
			//判断是否文件正在打开，如果打开则不能删除
			for (int i=0; i<openFiles.getFiles().size(); i++){
				if (openFiles.getFiles().get(i).getFile().equals(fat.getObject())){
					System.out.println("文件正打开着，不能删除");
					return 0;
				}
			}
			
			for (int i=0; i<myFAT.length; i++){
				if (myFAT[i] != null && myFAT[i].getType() == FileSystemUtil.FILE){
					if (((File)myFAT[i].getObject()).equals((File)fat.getObject())){
						myFAT[i] = null;
					}
				}
			}
			
		} else {
			//---------------->文件夹
			String path = ((Folder)fat.getObject()).getLocation();
			String folderPath = ((Folder)fat.getObject()).getLocation() + "\\" + ((Folder)fat.getObject()).getFolderName();
			int index = 0;
			for (int i=2; i<myFAT.length; i++){
				if (myFAT[i] != null){
					Object obj = myFAT[i].getObject();
					if (myFAT[i].getType() == FileSystemUtil.FOLDER){
						if (((Folder)obj).getLocation().equals(folderPath)){
							System.out.println("文件夹不为空，不能删除");
							return 0;
						}
					} else {
						if (((File)obj).getLocation().equals(folderPath)){
							System.out.println("文件夹不为空，不能删除");
							return 0;
						}
					}
					if (myFAT[i].getType() == FileSystemUtil.FOLDER){
						if (((Folder)myFAT[i].getObject()).equals((Folder)fat.getObject())){
							index = i;
						}
					}
				}
			}

			myFAT[index] = null;
			DefaultMutableTreeNode parentNode = map.get(path);
			parentNode.remove(map.get(folderPath));
			map.remove(folderPath);
		}
		return 1;
	}
	
	public static FAT[] getMyFAT() {
		return myFAT;
	}

	public static void setMyFAT(FAT[] myFAT) {
		FATService.myFAT = myFAT;
	}
	
	public static FAT getFAT(int index){
		return myFAT[index];
	}

	public static void catFile(int index){
		List<FAT> resultList = new ArrayList<>(myFAT.length);
		resultList.addAll(Arrays.asList(myFAT));
		String content = "";
		for (FAT fat: resultList) {
			File file = ((File) resultList.get(index).getObject());
			if (resultList.get(index).getIndex() != 255) {
//				content = content + file.getContent();
				content = content + RAFTestFactory.read(file.getDiskNum(),64);
				index = resultList.get(index).getIndex();
			}else {
//				content = content + file.getContent();
				content = content + RAFTestFactory.read(file.getDiskNum(),64);
				break;
			}
		}

		System.out.println("磁盘中文件内容：" + content);

	}
	
	public static OpenFiles getOpenFiles() {
		return openFiles;
	}

	public static void setOpenFiles(OpenFiles openFiles) {
		FATService.openFiles = openFiles;
	}

}
