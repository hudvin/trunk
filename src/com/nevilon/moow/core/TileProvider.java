package com.nevilon.moow.core;


import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class TileProvider {

	private LocalStorage localStorage = new LocalStorage();
	
	private TileLoader tileLoader = new TileLoader(this);
	
	private BitmapCache inMemoryCache = new BitmapCache();
	
	private PhysicMap physicMap;
	
	public TileProvider(PhysicMap physicMap){
		this.physicMap = physicMap;
	}
	
	public void getTile(RawTile tile, boolean useCache){
		// попытка загрузить из кеша
		Bitmap tmpBitmap;
		if(useCache){
			tmpBitmap = inMemoryCache.get(tile);
			if (tmpBitmap!=null){
				returnTile(tmpBitmap, tile);
			}
		}
		
		
		InputStream outStream = localStorage.get(tile);
		if(outStream!=null){
			tmpBitmap = BitmapFactory.decodeStream(outStream);
			inMemoryCache.put(tile, tmpBitmap);
			returnTile(tmpBitmap,tile);
		} else {
			tileLoader.load(tile);
		}
	}
	
	public void returnTile(Bitmap bitmap, RawTile tile){
		System.out.println(bitmap);
		physicMap.update(bitmap,tile);
	}
	
	public void putToStorage(RawTile tile,byte[]data){
		localStorage.put(tile,data);
	}
	
	
	
}
