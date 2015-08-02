/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import static pl.zumbrzycki.teezeerc.rx.Utils.getDefaultLeftPointByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getDefaultRightPointByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getLeftColorByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getRightColorByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.isPointInRectangle;
import static pl.zumbrzycki.teezeerc.rx.Utils.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Tomasz Zumbrzycki Android view class responsible for rendering sticks
 *         locations and intercepting user touch events
 * 
 */
public class MultitouchView extends View {

	// default values for full hd screen size
	static final int CIRCLE_SIZE = 80;
	static final int SQUARE_WIDTH = 508;
	static final int TOP_1 = 290;
	static final int LEFT_1 = 121;
	static final int BOTTOM_1 = 290 + SQUARE_WIDTH;
	static final int RIGHT_1 = 121 + SQUARE_WIDTH;
	static final int TOP_2 = 290;
	static final int LEFT_2 = 1292;
	static final int BOTTOM_2 = 290 + SQUARE_WIDTH;
	static final int RIGHT_2 = 1292 + SQUARE_WIDTH;

	// default mode, throttle on left side
	private static int MODE = 2;
	private boolean reverse[];

	private Map<Integer, PointF> mActivePointers;
	private Map<Integer, Integer> channelValues;
	private Paint mPaint;
	private Paint rectPaint;
	private Rect rectLeft, rectRight;
	private PointF defaultLeftPoint, defaultRightPoint;

	private SendPacketAsyncTask task;

	public MultitouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	@SuppressWarnings("unchecked")
	private void initView() {
		mActivePointers = new HashMap<Integer, PointF>();
		channelValues = new HashMap<Integer, Integer>();
		reverse = new boolean[] { false, true, false, true, false, false,
				false, false };
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.BLUE);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectPaint.setColor(Color.BLACK);
		rectPaint.setStrokeWidth(10);
		rectPaint.setStyle(Paint.Style.STROKE);
		rectLeft = new Rect(LEFT_1, TOP_1, RIGHT_1, BOTTOM_1);
		rectRight = new Rect(LEFT_2, TOP_2, RIGHT_2, BOTTOM_2);
		defaultLeftPoint = getDefaultLeftPointByMode(MODE);
		defaultRightPoint = getDefaultRightPointByMode(MODE);
		mActivePointers.put(0, defaultLeftPoint);
		mActivePointers.put(1, defaultRightPoint);
		mapPointersToChannelsByMode(MODE, mActivePointers, channelValues);
		task = new SendPacketAsyncTask("192.168.4.1", 1112, getContext());
		task.execute(channelValues);

	}

	/**
	 * 
	 * @param mode
	 *            - current flight mode
	 * @param pointers
	 *            - position of user fingers
	 * @param channels
	 *            - stick positions translated to receiver channel values
	 */
	private void mapPointersToChannelsByMode(int mode,
			Map<Integer, PointF> pointers, Map<Integer, Integer> channels) {
		PointF left, right;
		if (mode == 2) {
			left = pointers.get(0);
			right = pointers.get(1);
			if (!reverse[0]) {
				channels.put(1, map(right.x, LEFT_2, RIGHT_2, 0, 254));
			} else {
				channels.put(1, map(right.x, RIGHT_2, LEFT_2, 0, 254));
			}
			if (!reverse[1]) {
				channels.put(2, map(right.y, BOTTOM_2, TOP_2, 0, 254));
			} else {
				channels.put(2, map(right.y, TOP_2, BOTTOM_2, 0, 254));
			}
			if (!reverse[2]) {
				channels.put(3, map(left.y, BOTTOM_1, TOP_1, 0, 254));
			} else {
				channels.put(3, map(left.y, TOP_1, BOTTOM_1, 0, 254));
			}
			if (!reverse[3]) {
				channels.put(4, map(left.x, LEFT_1, RIGHT_1, 0, 254));
			} else {
				channels.put(4, map(left.x, RIGHT_1, LEFT_1, 0, 254));
			}
		} else {
			left = pointers.get(1);
			right = pointers.get(0);
			if (!reverse[0]) {
				channels.put(1, map(right.x, LEFT_1, RIGHT_1, 0, 254));
			} else {
				channels.put(1, map(right.x, RIGHT_1, LEFT_1, 0, 254));
			}
			if (!reverse[1]) {
				channels.put(2, map(right.y, BOTTOM_1, TOP_1, 0, 254));
			} else {
				channels.put(2, map(right.y, TOP_1, BOTTOM_1, 0, 254));
			}
			if (!reverse[2]) {
				channels.put(3, map(left.y, BOTTOM_2, TOP_2, 0, 254));
			} else {
				channels.put(3, map(left.y, TOP_2, BOTTOM_2, 0, 254));
			}
			if (!reverse[3]) {
				channels.put(4, map(left.x, LEFT_2, RIGHT_2, 0, 254));
			} else {
				channels.put(4, map(left.x, RIGHT_2, LEFT_2, 0, 254));
			}
		}

	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int pointerIndex = event.getActionIndex();
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			if (isPointInRectangle(f, rectLeft)) {
				mActivePointers.put(0, f);
			}
			if (isPointInRectangle(f, rectRight)) {
				mActivePointers.put(1, f);
			}
			mapPointersToChannelsByMode(MODE, mActivePointers, channelValues);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				PointF newPoint = new PointF(event.getX(i), event.getY(i));
				if (isPointInRectangle(newPoint, rectLeft)) {
					mActivePointers.remove(0);
					mActivePointers.put(0, newPoint);
				}
				if (isPointInRectangle(newPoint, rectRight)) {
					mActivePointers.remove(1);
					mActivePointers.put(1, newPoint);
				}
			}
			mapPointersToChannelsByMode(MODE, mActivePointers, channelValues);
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			if (isPointInRectangle(f, rectLeft)) {
				mActivePointers.remove(0);
				if (MODE == 2) {
					mActivePointers.put(0, new PointF(defaultLeftPoint.x, f.y));
				} else {
					mActivePointers.put(0, defaultLeftPoint);
				}
			}
			if (isPointInRectangle(f, rectRight)) {
				mActivePointers.remove(1);
				if (MODE == 2) {
					mActivePointers.put(1, defaultRightPoint);
				} else {
					mActivePointers
							.put(1, new PointF(defaultRightPoint.x, f.y));
				}
			}
			mapPointersToChannelsByMode(MODE, mActivePointers, channelValues);
			break;
		}
		}
		invalidate();
		return true;
	}

	/**
	 * Method draws stick positions and bounding rectangles on the screen.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Iterator it = mActivePointers.entrySet().iterator();
		PointF point = null;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			point = (PointF) pair.getValue();
			if (point != null) {
				if (isPointInRectangle(point, rectLeft)) {
					mPaint.setColor(getLeftColorByMode(MODE));
				}
				if (isPointInRectangle(point, rectRight)) {
					mPaint.setColor(getRightColorByMode(MODE));
				}
				canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, mPaint);
			}
		}
		canvas.drawRect(rectLeft, rectPaint);
		canvas.drawRect(rectRight, rectPaint);
	}

	public SendPacketAsyncTask getTask() {
		return task;
	}

	public void setTask(SendPacketAsyncTask task) {
		this.task = task;
	}

	public int getMODE() {
		return MODE;
	}

	public void setMODE(int mode) {
		MODE = mode;
	}

}