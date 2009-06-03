package com.nevilon.moow.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class LocalStorage {

	private static final String root_dir_location = "/sdcard/moow/";
	
	public LocalStorage() {
		init();
	}

	public void clear() {
		File dir = new File(root_dir_location);
		deleteDir(dir);
	}

	private void init() {
		File dir = new File(root_dir_location);
		if (!(dir.exists() && dir.isDirectory())) {
			dir.mkdirs();
		}
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private String buildPath(int x, int y, int z) {
		String intPath = String.valueOf(z) + String.valueOf(x)
				+ String.valueOf(y);
		StringBuffer path = new StringBuffer();
		path.append(root_dir_location);
		for (int i = 0; i < intPath.length(); i++) {
			path.append(intPath.charAt(i));
			path.append("/");
		}
		return path.toString();
	}

	public void put(RawTile tile, byte[] data) {
		String path = buildPath(tile.getX(),tile.getY(), tile.getZ());
		File fullPath = new File(path);
		fullPath.mkdirs();
		fullPath = new File(path + "tile.tl");
		try {
			BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(fullPath), 65536);
			outStream.write(data);
			outStream.flush();
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public BufferedInputStream get(RawTile tile) {
		String path = buildPath(tile.getX(), tile.getY(), tile.getZ());
		File tileFile = new File(path + "/tile.tl");
		if (tileFile.exists()){
			try {
				return new BufferedInputStream(new FileInputStream(tileFile),65536);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} 
		return null;
	}

}
