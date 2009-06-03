package com.nevilon.bigplanet.core;

import android.graphics.Bitmap;

import com.nevilon.bigplanet.core.loader.TileLoader;
import com.nevilon.bigplanet.core.providers.MapStrategy;
import com.nevilon.bigplanet.core.providers.MapStrategyFactory;
import com.nevilon.bigplanet.core.storage.BitmapCacheWrapper;
import com.nevilon.bigplanet.core.storage.LocalStorageWrapper;

public class TileResolver {

	private TileLoader tileLoader;

	private PhysicMap physicMap;

	private BitmapCacheWrapper cacheProvider = BitmapCacheWrapper.getInstance();

	private Handler scaledHandler;

	private Handler localLoaderHandler;

	private int strategyId = -1;

	
	public TileResolver(final PhysicMap physicMap) {
		this.physicMap = physicMap;
		tileLoader = new TileLoader(
		// обработчик загрузки тайла с сервера
				new Handler() {
					@Override
					public void handle(RawTile tile, byte[] data) {
						if (tile.s == strategyId) {
							LocalStorageWrapper.put(tile, data);
							Bitmap bmp = LocalStorageWrapper.get(tile);
							cacheProvider.putToCache(tile, bmp);
							updateMap(tile, bmp);
						}
					}
				});
		new Thread(tileLoader).start();

		// обработчик загрузки скалированых картинок
		this.scaledHandler = new Handler() {

			@Override
			public synchronized void handle(RawTile tile, Bitmap bitmap,
					boolean isScaled) {
				if (bitmap != null && tile.s == strategyId) {
					updateMap(tile, bitmap);
					if (isScaled) {
						cacheProvider.putToScaledCache(tile, bitmap);
					}
				}
						}

		};
		// обработчик загрузки с дискового кеша
		this.localLoaderHandler = new Handler() {

			@Override
			public synchronized void handle(RawTile tile, Bitmap bitmap,
					boolean isScaled) {
				if (tile.s != strategyId) { // если не текущий провайдер
					return;
				} 
				if (bitmap != null) { // если тайл есть в файловом кеше
					updateMap(tile, bitmap);
					cacheProvider.putToCache(tile, bitmap);
				} else {  // если тайла нет в файловом кеше
					
					bitmap = cacheProvider.getScaledTile(tile);
					if (bitmap == null) {
						new Thread(new TileScaler(tile, scaledHandler)).start();

					} else { // скалированый тайл из кеша
						updateMap(tile, bitmap);

					}
					load(tile);
				}
			}

		};

	}

	private void load(RawTile tile) {
		if (tile.s != -1) {
			tileLoader.load(tile);
		}
	}

	private void updateMap(RawTile tile, Bitmap bitmap) {
		physicMap.update(bitmap, tile);
	}

	/**
	 * Загружает заданный тайл
	 * 
	 * @param tile
	 * @return
	 */
	public void getTile(final RawTile tile, boolean useCache) {
		Bitmap bitmap = null;
		if (useCache) {
			bitmap = cacheProvider.getTile(tile);
			if (bitmap != null) {
				// возврат тайла
				updateMap(tile, bitmap);
				return;
			} else {
				LocalStorageWrapper.get(tile, localLoaderHandler);
			}
		} else {
			LocalStorageWrapper.get(tile, localLoaderHandler);
		}
	}

	public synchronized void setMapSource(int sourceId) {
		cacheProvider.clear();
		System.gc();
		MapStrategy mapStrategy = MapStrategyFactory.getStrategy(sourceId);
		this.strategyId = sourceId;
		tileLoader.setMapStrategy(mapStrategy);
	}

	public int getMapSourceId() {
		return this.strategyId;
	}

	public void setUseNet(boolean useNet) {
		tileLoader.setUseNet(useNet);
		if (useNet) {
			physicMap.reloadTiles();
		}
	}

}
