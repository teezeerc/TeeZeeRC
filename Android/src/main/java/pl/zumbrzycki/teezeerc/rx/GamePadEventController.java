package pl.zumbrzycki.teezeerc.rx;

import static pl.zumbrzycki.teezeerc.rx.MultitouchView.BOTTOM_1;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.BOTTOM_2;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.LEFT_1;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.LEFT_2;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.RIGHT_1;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.RIGHT_2;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.TOP_1;
import static pl.zumbrzycki.teezeerc.rx.MultitouchView.TOP_2;
import static pl.zumbrzycki.teezeerc.rx.Utils.mapValueToRange;

import java.util.Map;

import android.graphics.PointF;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

public class GamePadEventController {

	public static boolean onGenericMotionEvent(MotionEvent event,
			Map<Integer, PointF> pointers) {

		// Check that the event came from a game controller
		Log.d("stick", "view:" + event.getSource());
		if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
				&& event.getAction() == MotionEvent.ACTION_MOVE) {

			// Process all historical movement samples in the batch
			final int historySize = event.getHistorySize();

			// Process the movements starting from the
			// earliest historical position in the batch
			for (int i = 0; i < historySize; i++) {
				// Process the event at historical position i
				processJoystickInput(event, i, pointers);
			}

			// Process the current movement sample in the batch (position -1)
			processJoystickInput(event, -1, pointers);
			return true;
		}
		return false;
	}

	private static float getCenteredAxis(MotionEvent event, InputDevice device,
			int axis, int historyPos) {
		final InputDevice.MotionRange range = device.getMotionRange(axis,
				event.getSource());

		// A joystick at rest does not always report an absolute position of
		// (0,0). Use the getFlat() method to determine the range of values
		// bounding the joystick axis center.
		if (range != null) {
			final float flat = range.getFlat();
			final float value = historyPos < 0 ? event.getAxisValue(axis)
					: event.getHistoricalAxisValue(axis, historyPos);

			// Ignore axis values that are within the 'flat' region of the
			// joystick axis center.
			if (Math.abs(value) > flat) {
				return value;
			}
		}
		return 0;
	}

	private static void processJoystickInput(MotionEvent event, int historyPos,
			Map<Integer, PointF> pointers) {

		InputDevice mInputDevice = event.getDevice();

		// Calculate the horizontal distance to move by
		// using the input value from one of these physical controls:
		// the left control stick, hat axis, or the right control stick.
		float x1 = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_X,
				historyPos);
		float y1 = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Y,
				historyPos);

		float x2 = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_Z,
				historyPos);
		float y2 = getCenteredAxis(event, mInputDevice, MotionEvent.AXIS_RZ,
				historyPos);

		final InputDevice.MotionRange axisXRange = mInputDevice.getMotionRange(
				MotionEvent.AXIS_X, event.getSource());
		final InputDevice.MotionRange axisYRange = mInputDevice.getMotionRange(
				MotionEvent.AXIS_Y, event.getSource());
		final InputDevice.MotionRange axisZRange = mInputDevice.getMotionRange(
				MotionEvent.AXIS_Z, event.getSource());
		final InputDevice.MotionRange axisRZRange = mInputDevice
				.getMotionRange(MotionEvent.AXIS_RZ, event.getSource());

		// Calculate the vertical distance to move by
		// using the input value from one of these physical controls:
		// the left control stick, hat switch, or the right control stick.

		Log.d("stick",
				"axis_x:" + axisXRange.getMin() + " " + axisXRange.getMax()
						+ " axis_y" + axisYRange.getMin() + " "
						+ axisYRange.getMax());
		Log.d("stick",
				"axis_z:" + axisZRange.getMin() + " " + axisZRange.getMax()
						+ " axis_rz" + axisRZRange.getMin() + " "
						+ axisRZRange.getMax());
		Log.d("stick", x1 + ":" + y1 + " " + x2 + ":" + y2);
		// Update the ship object based on the new x and y values
		float leftPointX = mapValueToRange(x1, axisXRange.getMin(),
				axisXRange.getMax(), LEFT_1, RIGHT_1);
		float leftPointY = mapValueToRange(y1, axisYRange.getMin(),
				axisYRange.getMax(), TOP_1, BOTTOM_1);
		float rightPointX = mapValueToRange(x2, axisZRange.getMin(),
				axisZRange.getMax(), LEFT_2, RIGHT_2);
		float rightPointY = mapValueToRange(y2, axisRZRange.getMin(),
				axisRZRange.getMax(), TOP_2, BOTTOM_2);

		pointers.remove(0);
		pointers.put(0, new PointF(leftPointX, leftPointY));
		pointers.remove(1);
		pointers.put(1, new PointF(rightPointX, rightPointY));

	}
}
