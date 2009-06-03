package com.nevilon.moow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nevilon.moow.core.PhysicMap;

public class DrawTesting extends Activity {
	public static final int DIRECTION_RIGHT = 0, DIRECTION_LEFT = 1;

	private Panel main;

	private volatile boolean running = true;

	private boolean inMove = false;
	private boolean alreadyX = false;

	private int nx = -1;
	
	private Point previousMovePoint = new Point();
	private Point nextMovePoint = new Point();
	private Point globalOffset = new Point();

	PhysicMap pmap = new PhysicMap();

	boolean moving = false;

	Bitmap[][] tiles = new Bitmap[4][4];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = new Panel(this);
		setContentView(main, new ViewGroup.LayoutParams(320, 480));
		(new Thread(new AnimationLoop())).start();
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			nextMovePoint.set((int) event.getX(), (int) event.getY());
			inMove = true;
			alreadyX = false;
			break;
		case MotionEvent.ACTION_MOVE:
			previousMovePoint.set(nextMovePoint.x, nextMovePoint.y);
			nextMovePoint.set((int) event.getX(), (int) event.getY());
			globalOffset.set(globalOffset.x
					+ (nextMovePoint.x - previousMovePoint.x), globalOffset.y
					+ (nextMovePoint.y - previousMovePoint.y));
			if (globalOffset.x%256 == 0){
				System.out.println("blyaaaaaaa");
			}
			break;
		case MotionEvent.ACTION_UP:
			inMove = false;
			break;
		}

		return super.onTouchEvent(event);
	}

	private synchronized void doDraw(Canvas canvas, Paint paint) {
		Bitmap tmpBitmap;
		
		/*
		 * сравнение прошлого отступа и текущего
		 * если отличаются на 1 - перемещение
		 */
		if (inMove &&
				nx!=-1 && Math.abs(Math.ceil((globalOffset.x+256)/256)-nx)==1
				){
			globalOffset.x -= 256;
			 
			pmap.moveLeft();
		 nx = -1;
		} else {
			nx= (int) Math.ceil(globalOffset.x/256);
			
		}
		/*
		System.out.println(Math.ceil(globalOffset.x/256));
		if (inMove && !alreadyX && (float) globalOffset.x % 256 == 0) {
			pmap.moveLeft();
			alreadyX = true;
			globalOffset.x -= 256;
			System.out.println("border");
		}
		*/
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tmpBitmap = pmap.getCells()[i][j];
				if (tmpBitmap != null) {
					// int direction = nextMovePoint.x-previousMovePoint.x;
					// int ox = (pmap.getDefaultTile().getX()==0 &&
					// direction>0)?0:globalOffset.x;
					// System.out.println(pmap.getDefaultTile().getX());
					canvas.drawBitmap(tmpBitmap, (i) * 256 + globalOffset.x,
							(j) * 256 + globalOffset.y, paint);

				}
			}
		}

	}

	class Panel extends View {
		Paint paint;

		public Panel(Context context) {
			super(context);
			paint = new Paint();
			TextView txt = new TextView(getContext());
			txt.setText("xxxxxxxx");
			addContentView(txt, new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		@Override
		protected void  onDraw(Canvas canvas) {
			super.onDraw(canvas);
			doDraw(canvas, paint);
		}
	}

	class AnimationLoop implements Runnable {
		public void run() {
			while (true) {
				while (running) {
					try {
						Thread.sleep(30);
					} catch (InterruptedException ex) {
					}
					main.postInvalidate();
				}
			}
		}
	}
}