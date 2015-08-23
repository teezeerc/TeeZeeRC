/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import static pl.zumbrzycki.teezeerc.rx.Utils.getDefaultLeftPointByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getDefaultRightPointByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getLeftColorByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.getRightColorByMode;
import static pl.zumbrzycki.teezeerc.rx.Utils.isPointInRectangle;
import static pl.zumbrzycki.teezeerc.rx.Utils.mapValueToRange;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Tomasz Zumbrzycki Android view class responsible for rendering sticks
 *         locations and intercepting user touch events
 * 
 */
public class MultitouchView extends View {

	enum Mode {
		Mode_2, Mode_1
	}

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
	private static Mode SELECTED_MODE = Mode.Mode_2;
	private boolean reverseChannels[];

	private Map<Integer, PointF> pointers;
	private Map<Integer, Integer> channelValues;
	private Paint circlePaint;
	private Paint rectPaint;
	private Rect rectLeft, rectRight;
	private PointF defaultLeftPoint, defaultRightPoint;

	private SendPacketAsyncTask task;

	public MultitouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setupEmptyPointersAndChannels();
		setupReverseForChannels();
		setupDefaultPaintStyle();// TODO
		setupRectanglesPaintStyle();
		setupRectanglesDimensions();
		setupDefaultSticksPositionsByMode(SELECTED_MODE);
		mapPointersToChannelsByMode(SELECTED_MODE, pointers, channelValues);
		runAsyncTask();
	}

	private void setupDefaultPaintStyle() {
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(Color.BLUE);
		circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	private void setupEmptyPointersAndChannels() {
		pointers = new HashMap<Integer, PointF>();
		channelValues = new HashMap<Integer, Integer>();
	}

	private void setupDefaultSticksPositionsByMode(Mode mode) {
		defaultLeftPoint = getDefaultLeftPointByMode(mode);
		defaultRightPoint = getDefaultRightPointByMode(mode);
		pointers.put(0, defaultLeftPoint);
		pointers.put(1, defaultRightPoint);
	}

	private void setupReverseForChannels() {
		reverseChannels = new boolean[] { false, true, false, true, false,
				false, false, false };
	}

	private void setupRectanglesDimensions() {
		rectLeft = new Rect(LEFT_1, TOP_1, RIGHT_1, BOTTOM_1);
		rectRight = new Rect(LEFT_2, TOP_2, RIGHT_2, BOTTOM_2);
	}

	private void setupRectanglesPaintStyle() {
		rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectPaint.setColor(Color.BLACK);
		rectPaint.setStrokeWidth(10);
		rectPaint.setStyle(Paint.Style.STROKE);
	}

	@SuppressWarnings("unchecked")
	private void runAsyncTask() {
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
	private void mapPointersToChannelsByMode(Mode mode,
			Map<Integer, PointF> pointers, Map<Integer, Integer> channels) {
		PointF left, right;
		if (mode == Mode.Mode_2) {
			left = pointers.get(0);
			right = pointers.get(1);
			mapChannelOneValueForMode2(channels, reverseChannels, right);
			mapChannelTwoValueForMode2(channels, reverseChannels, right);
			mapChannelThreeValueForMode2(channels, reverseChannels, left);
			mapChannelFourValueForMode2(channels, reverseChannels, left);
		} else {
			left = pointers.get(1);
			right = pointers.get(0);
			mapChannelOneValueForMode1(channels, reverseChannels, right);
			mapChannelTwoValueForMode1(channels, reverseChannels, right);
			mapChannelThreeValueForMode1(channels, reverseChannels, left);
			mapChannelFourValueForMode1(channels, reverseChannels, left);
		}

	}

	private void mapChannelFourValueForMode1(Map<Integer, Integer> channels,
			boolean[] reverse, PointF left) {
		if (!reverse[3]) {
			channels.put(4, mapValueToRange(left.x, LEFT_2, RIGHT_2, 0, 254));
		} else {
			channels.put(4, mapValueToRange(left.x, RIGHT_2, LEFT_2, 0, 254));
		}
	}

	private void mapChannelThreeValueForMode1(Map<Integer, Integer> channels,
			boolean[] reverse, PointF left) {
		if (!reverse[2]) {
			channels.put(3, mapValueToRange(left.y, BOTTOM_2, TOP_2, 0, 254));
		} else {
			channels.put(3, mapValueToRange(left.y, TOP_2, BOTTOM_2, 0, 254));
		}
	}

	private void mapChannelTwoValueForMode1(Map<Integer, Integer> channels,
			boolean[] reverse, PointF right) {
		if (!reverse[1]) {
			channels.put(2, mapValueToRange(right.y, BOTTOM_1, TOP_1, 0, 254));
		} else {
			channels.put(2, mapValueToRange(right.y, TOP_1, BOTTOM_1, 0, 254));
		}
	}

	private void mapChannelOneValueForMode1(Map<Integer, Integer> channels,
			boolean[] reverse, PointF right) {
		if (!reverse[0]) {
			channels.put(1, mapValueToRange(right.x, LEFT_1, RIGHT_1, 0, 254));
		} else {
			channels.put(1, mapValueToRange(right.x, RIGHT_1, LEFT_1, 0, 254));
		}
	}

	private void mapChannelFourValueForMode2(Map<Integer, Integer> channels,
			boolean[] reverse, PointF left) {
		if (!reverse[3]) {
			channels.put(4, mapValueToRange(left.x, LEFT_1, RIGHT_1, 0, 254));
		} else {
			channels.put(4, mapValueToRange(left.x, RIGHT_1, LEFT_1, 0, 254));
		}
	}

	private void mapChannelThreeValueForMode2(Map<Integer, Integer> channels,
			boolean[] reverse, PointF left) {
		if (!reverse[2]) {
			channels.put(3, mapValueToRange(left.y, BOTTOM_1, TOP_1, 0, 254));
		} else {
			channels.put(3, mapValueToRange(left.y, TOP_1, BOTTOM_1, 0, 254));
		}
	}

	private void mapChannelTwoValueForMode2(Map<Integer, Integer> channels,
			boolean[] reverse, PointF right) {
		if (!reverse[1]) {
			channels.put(2, mapValueToRange(right.y, BOTTOM_2, TOP_2, 0, 254));
		} else {
			channels.put(2, mapValueToRange(right.y, TOP_2, BOTTOM_2, 0, 254));
		}
	}

	private void mapChannelOneValueForMode2(Map<Integer, Integer> channels,
			boolean[] reverse, PointF right) {
		if (!reverse[0]) {
			channels.put(1, mapValueToRange(right.x, LEFT_2, RIGHT_2, 0, 254));
		} else {
			channels.put(1, mapValueToRange(right.x, RIGHT_2, LEFT_2, 0, 254));
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("stick", "" + event.getSource());
		int pointerIndex = event.getActionIndex();
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			if (isPointInRectangle(f, rectLeft)) {
				pointers.put(0, f);
			}
			if (isPointInRectangle(f, rectRight)) {
				pointers.put(1, f);
			}
			mapPointersToChannelsByMode(SELECTED_MODE, pointers, channelValues);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				PointF newPoint = new PointF(event.getX(i), event.getY(i));
				if (isPointInRectangle(newPoint, rectLeft)) {
					pointers.remove(0);
					pointers.put(0, newPoint);
				}
				if (isPointInRectangle(newPoint, rectRight)) {
					pointers.remove(1);
					pointers.put(1, newPoint);
				}
			}
			mapPointersToChannelsByMode(SELECTED_MODE, pointers, channelValues);
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			if (isPointInRectangle(f, rectLeft)) {
				pointers.remove(0);
				if (SELECTED_MODE == Mode.Mode_2) {
					pointers.put(0, new PointF(defaultLeftPoint.x, f.y));
				} else {
					pointers.put(0, defaultLeftPoint);
				}
			}
			if (isPointInRectangle(f, rectRight)) {
				pointers.remove(1);
				if (SELECTED_MODE == Mode.Mode_2) {
					pointers.put(1, defaultRightPoint);
				} else {
					pointers.put(1, new PointF(defaultRightPoint.x, f.y));
				}
			}
			mapPointersToChannelsByMode(SELECTED_MODE, pointers, channelValues);
			break;
		}
		}
		invalidate();
		return true;
	}

	/**
	 * Method draws stick positions and bounding rectangles on the screen.
	 */

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawStickPositions(canvas, pointers);
		drawBoundingRectangles(canvas);
	}

	private void drawBoundingRectangles(Canvas canvas) {
		canvas.drawRect(rectLeft, rectPaint);
		canvas.drawRect(rectRight, rectPaint);
	}

	@SuppressWarnings("rawtypes")
	private void drawStickPositions(Canvas canvas, Map<Integer, PointF> pointers) {
		Iterator it = pointers.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			PointF point = (PointF) pair.getValue();
			if (point != null) {
				if (isPointInRectangle(point, rectLeft)) {
					circlePaint.setColor(getLeftColorByMode(SELECTED_MODE));
				}
				if (isPointInRectangle(point, rectRight)) {
					circlePaint.setColor(getRightColorByMode(SELECTED_MODE));
				}
				canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, circlePaint);
			}
		}
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		boolean result = GamePadEventController.onGenericMotionEvent(event,
				pointers);
		mapPointersToChannelsByMode(SELECTED_MODE, pointers, channelValues);
		invalidate();
		return result || super.onGenericMotionEvent(event);
	}

	public SendPacketAsyncTask getTask() {
		return task;
	}

	public void setTask(SendPacketAsyncTask task) {
		this.task = task;
	}

	public Mode getMODE() {
		return SELECTED_MODE;
	}

	public void setMODE(Mode mode) {
		SELECTED_MODE = mode;
	}

}