/*
 *  GNU GENERAL PUBLIC LICENSE Version 2, June 1991
 */
package pl.zumbrzycki.teezeerc.rx;

import static pl.zumbrzycki.teezeerc.rx.Utils.constructPacket;
import static pl.zumbrzycki.teezeerc.rx.Utils.printPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * @author Tomasz Zumbrzycki Class is responsible for sending UDP packets to
 *         TeeZeeRC receiver board and for displaying current WiFi connection
 *         status.
 */
public class SendPacketAsyncTask extends
		AsyncTask<Map<Integer, Integer>, String, String> {

	private String ipAddress;
	private int port;
	private Context context;
	private TextView view;

	public SendPacketAsyncTask(String ipAddress, int port, Context context) {
		super();
		this.ipAddress = ipAddress;
		this.port = port;
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Method responsible for sending UDP packets to RX and for updating WiFi state information
	 * 
	 */
	protected String doInBackground(Map<Integer, Integer>... params) {

		Log.d("map", "task started");
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			while (!isCancelled()) {
				String result = checkWifi();
				publishProgress(result);
				// printMap(params[0]);
				try {
					byte[] packet = constructPacket(params[0]);
					printPacket(packet);
					InetAddress address = InetAddress.getByName(ipAddress);
					DatagramPacket udppacket = new DatagramPacket(packet,
							packet.length, address, port);
					datagramSocket.send(udppacket);
					Thread.sleep(100);
				} catch (InterruptedException | IOException e) {
					Log.e("task", "exception", e);
				}
			}
			Log.d("map", "task cancelled");
			datagramSocket.close();
		} catch (IOException e) {
			Log.e("task", "exception", e);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		if (values.length > 0 && view != null) {
			view.setText(values[0]);
		}
	}

	/**
	 * Method checks state of WiFi connection and if it detects low signal level
	 * - vibration is introduced
	 * 
	 * @return
	 */
	private String checkWifi() {
		String result = "";
		ConnectivityManager cm = (ConnectivityManager) getContext()
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo Info = cm.getActiveNetworkInfo();
		if (Info == null || !Info.isConnectedOrConnecting()) {
			Log.d("WIFI CONNECTION", "No connection");
			result = "No connection!";
		} else {
			int netType = Info.getType();
			if (netType == ConnectivityManager.TYPE_WIFI) {
				WifiManager wifiManager = (WifiManager) getContext()
						.getApplicationContext().getSystemService(
								Context.WIFI_SERVICE);
				int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
				int rssi = wifiManager.getConnectionInfo().getRssi();
				int wifiLevel = WifiManager.calculateSignalLevel(rssi, 100);
				if (wifiLevel < 20) {
					Vibrator v = (Vibrator) getContext().getSystemService(
							Context.VIBRATOR_SERVICE);
					v.vibrate(500);
				}
				Log.d("WIFI CONNECTION", "Wifi connection speed: " + linkSpeed
						+ " rssi: " + rssi + " wifilevel:" + wifiLevel);
				result = "Speed: " + linkSpeed + " wifilevel:" + wifiLevel;
			}
		}
		return result;
	}

	public String getAddress() {
		return ipAddress;
	}

	public void setAddress(String address) {
		this.ipAddress = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public View getView() {
		return view;
	}

	public void setView(TextView view) {
		this.view = view;
	}
}
