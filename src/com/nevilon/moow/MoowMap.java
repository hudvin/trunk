package com.nevilon.moow;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.nevilon.moow.core.PhysicMap;
import com.nevilon.moow.core.RawTile;
import com.nevilon.moow.core.ui.DoubleClickHelper;

public class MoowMap extends Activity {

	private final static int BCG_CELL_SIZE = 16; 
	
	private Panel main;

	private volatile boolean running = true;

	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	
	private  int zoom = 12; // whole world

	private PhysicMap pmap = new PhysicMap(new RawTile(9, 7, zoom));
	
	boolean inMove = false;
	
	private DoubleClickHelper dcDispatcher = new DoubleClickHelper();

	private Bitmap mapBg = drawBackground();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new CanvasUpdater())).start();

		class ZoomPanel extends RelativeLayout {

			public ZoomPanel(Context context) {
				super(context);
				ZoomControls zc = new ZoomControls(getContext());
				zc.setOnZoomOutClickListener(new OnClickListener(){
					public void onClick(View v) {
						pmap.zoomOut();
					}
				});
				zc.setOnZoomInClickListener(new OnClickListener() {
					public void onClick(View v) {
						pmap.zoomInCenter();
					}
				});
				addView(zc);
				setPadding(80, 368, 0, 0);
			}

		}
		addContentView(new ZoomPanel(MoowMap.this), new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	
	
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		// опускание клавиши
		case MotionEvent.ACTION_DOWN:
			inMove = false;
			nextMovePoint.set((int) event.getX(), (int) event.getY());	
			break;	
		// движение
		case MotionEvent.ACTION_MOVE:
			inMove = true;
			moveCoordinates(event.getX(), event.getY());
			break;
		// поднятие клавиши
		case MotionEvent.ACTION_UP:
			System.out.println("UP");
			if(inMove){
				moveCoordinates(event.getX(), event.getY());
			    quickHack();
			    quickHack();
			} else {
			   if(dcDispatcher.process(event)){
					pmap.zoomIn((int)event.getX(), (int)event.getY());				
				}
			}
			break;
		}

		return super.onTouchEvent(event);
	}

	
	private  void  quickHack(){
		int dx = 0,dy = 0;
	    if(pmap.globalOffset.x>0){
	    	dx = Math.round((pmap.globalOffset.x+320)/256);
	    } else {
	    	dx = Math.round((pmap.globalOffset.x)/256);
	    }
	    
	    
	    
	    
	    if(pmap.globalOffset.y>0){
	    	dy = (int)Math.floor((pmap.globalOffset.y+480)/256);  
	    } else {
	    	dy = (int)Math.floor(pmap.globalOffset.y/256);
		    
	    }
	    
	    pmap.globalOffset.x = pmap.globalOffset.x - dx*256 ;
	    pmap.globalOffset.y = pmap.globalOffset.y - dy*256;
	    
	    
	    pmap.move(dx, dy);
	    
	  
	    
	}
	
	
	
		
	
	
	private void moveCoordinates(float x, float y) {
		previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
		nextMovePoint.set((int) x, (int) y);
		pmap.globalOffset.set(pmap.globalOffset.x
				+ (nextMovePoint.x - previousMovePoint.x), pmap.globalOffset.y
				+ (nextMovePoint.y - previousMovePoint.y));
	}

	/**
	 * Рисует фон для карты( в клетку )
	 * @return
	 */
	private Bitmap drawBackground(){
		// создание битмапа по размеру экрана
		Bitmap bitmap = Bitmap.createBitmap(320, 480, Config.RGB_565);
		Canvas cv = new Canvas(bitmap);
		//прорисовка фона
	 	Paint background = new Paint();
	    background.setARGB(255, 128, 128, 128);
	    cv.drawRect(0, 0, 320, 480, background);
	    background.setAntiAlias(true);
	    //установка цвета линий
	    background.setColor(Color.WHITE);
	    // продольные линии
	    for (int i=0;i<320/MoowMap.BCG_CELL_SIZE;i++){
	    	cv.drawLine(MoowMap.BCG_CELL_SIZE*i, 0, MoowMap.BCG_CELL_SIZE*i, 480, background);   
	    }
	    // поперечные линии
	    for (int i=0;i<480/MoowMap.BCG_CELL_SIZE;i++){
	    	cv.drawLine(0, MoowMap.BCG_CELL_SIZE*i,  320,MoowMap.BCG_CELL_SIZE*i, background);
	    }
	    return bitmap;
	}
	
	private synchronized void doDraw(Canvas canvas, Paint paint) {
		Bitmap tmpBitmap;
		canvas.drawBitmap(mapBg,0,0,paint);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					canvas.drawBitmap(tmpBitmap, (i) * 256 + pmap.globalOffset.x,
							(j) * 256 + pmap.globalOffset.y, paint);
				}
			}
		}

	}

	

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}
	}

	
	class CanvasUpdater implements Runnable {

		public void run() {
			while (running) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
				}
				main.postInvalidate();
			}
		}

	}
}