/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import java.nio.ByteBuffer;
import java.util.Map;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

/**
 * @author Tomasz Zumbrzycki
 * Class contains utility methods used to:
 * construct UDP packets
 * calculate fletcher16 checksum for packet
 * get default values based on current flight mode (MODE 1 or 2)
 * print debug information 
 *
 */
public class Utils {

	public static boolean isPointInRectangle(PointF point, Rect rect) {
		if (point != null
				&& (point.x >= rect.left && point.x <= rect.right
						&& point.y >= rect.top && point.y <= rect.bottom)) {
			return true;
		} else {
			return false;
		}
	}

	public static int getLeftColorByMode(int mode) {
		if (mode == 1) {
			return Color.GREEN;
		} else {
			return Color.RED;
		}
	}

	public static int getRightColorByMode(int mode) {
		if (mode == 1) {
			return Color.RED;
		} else {
			return Color.GREEN;
		}

	}

	public static PointF getDefaultLeftPointByMode(int mode) {
		if (mode == 1) {
			return new PointF(MultitouchView.LEFT_1
					+ MultitouchView.SQUARE_WIDTH / 2, MultitouchView.TOP_1
					+ MultitouchView.SQUARE_WIDTH / 2);
		} else {
			return new PointF(MultitouchView.LEFT_1
					+ MultitouchView.SQUARE_WIDTH / 2, MultitouchView.BOTTOM_1);
		}
	}

	public static PointF getDefaultRightPointByMode(int mode) {
		if (mode == 1) {
			return new PointF(MultitouchView.LEFT_2
					+ MultitouchView.SQUARE_WIDTH / 2, MultitouchView.BOTTOM_2);
		} else {
			return new PointF(MultitouchView.LEFT_2
					+ MultitouchView.SQUARE_WIDTH / 2, MultitouchView.TOP_2
					+ MultitouchView.SQUARE_WIDTH / 2);
		}
	}

	public static int map(float value, float minActualInterval,
			float maxActualInterval, float minDesiredInterval,
			float maxDesiredInterval) {

		return Math
				.round(((value - minActualInterval) / (maxActualInterval - minActualInterval))
						* (maxDesiredInterval - minDesiredInterval)
						+ minDesiredInterval);

	}

	public static byte[] constructPacket(Map<Integer, Integer> channels) {
		byte[] tmp = new byte[9];
		tmp[0] = 1; // regular packet type
		tmp[1] = (byte) (int) channels.get(1);
		tmp[2] = (byte) (int) channels.get(2);
		tmp[3] = (byte) (int) channels.get(3);
		tmp[4] = (byte) (int) channels.get(4);
		tmp[5] = (byte) (int) 0;
		tmp[6] = (byte) (int) 0;
		tmp[7] = (byte) (int) 0;
		tmp[8] = (byte) (int) 0;
		byte[] checksum = fletcher16(tmp);
		byte[] packet = new byte[11];
		// actual packet construction
		packet[0] = tmp[0];
		packet[1] = tmp[1];
		packet[2] = tmp[2];
		packet[3] = tmp[3];
		packet[4] = tmp[4];
		packet[5] = tmp[5];
		packet[6] = tmp[6];
		packet[7] = tmp[7];
		packet[8] = tmp[8];
		packet[9] = checksum[0];
		packet[10] = checksum[1];
		return packet;
	}

	public static byte[] fletcher16(byte[] data) {
		char sum1 = 0;
		char sum2 = 0;
		char modulus = 255;

		for (int i = 0; i < data.length; i++) {
			sum1 = (char) ((sum1 + (char) data[i]) % modulus);
			sum2 = (char) ((sum2 + sum1) % modulus);
		}
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putChar((char) ((sum2 << 8) | sum1));
		return buffer.array();
	}

	@SuppressWarnings("rawtypes")
	public static void printMap(Map map) {
		StringBuilder msg = new StringBuilder();
		msg.append(map.get(1));
		msg.append(" ");
		msg.append(map.get(2));
		msg.append(" ");
		msg.append(map.get(3));
		msg.append(" ");
		msg.append(map.get(4));
		Log.d("map", msg.toString());
	}

	public static void printPacket(byte[] packet) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < packet.length; i++) {
			msg.append(String.format("%02X ", (packet[i] & 0xFF)));
			msg.append(" ");
		}
		Log.d("packet", msg.toString());
	}
}
