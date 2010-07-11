package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import android.util.Log;
import org.json.*;

public class Comms {
	// variables:
	private List<InetAddress> iplist = new ArrayList<InetAddress>();
	private InetAddress broadcastIP;
	private boolean authenticated = false;

	public Comms(WifiManager wifimanobj) {

		try {
			DhcpInfo dhcp = wifimanobj.getDhcpInfo();
			if (dhcp == null) {
				Log.d("shit", "Could not get dhcp info");
			}
			int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
			byte[] quads = new byte[4];
			for (int k = 0; k < 4; k++)
				quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			broadcastIP = InetAddress.getByAddress(quads);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

	}

	@SuppressWarnings("unused")
	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}
	
	public void discover() throws Exception {

		
		
		if (broadcastIP != null) {
			Log.e("discovery", "shit the bed..");
		}

		try {
			DatagramSocket clientSocket = new DatagramSocket();
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = "hello".getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, broadcastIP, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			clientSocket.setSoTimeout(5000); // sets how long reicive() blocks
												// for once there are no new
												// packates
			long t = System.currentTimeMillis();
			long end = t + 3000;
			while (System.currentTimeMillis() < end) {
				clientSocket.receive(receivePacket);
				iplist.add(receivePacket.getAddress());
				Log.e("discovery", (receivePacket.getAddress().toString()));
				Thread.sleep(500);
				// modifiedSentence = modifiedSentence +
				// receivePacket.getAddress().toString();
				Log.e("discovery1", String.valueOf(iplist.size()));
			}
			clientSocket.close();

		} catch (UnknownHostException e) {
			Log.e("recieved:", "UnknownHostException:" + e.toString());

		} catch (IOException e) {
			Log.e("recieved:", "IOException:" + e.toString());
		}
	}

	public boolean pair(InetAddress server) {
		try {

			String confirm = null;
			String mac, pkey;
			String reply = doSend("pair");
			JSONObject object = (JSONObject) new JSONTokener(reply).nextValue();
			confirm = object.getString("confirm");
			mac = object.getString("mac");
			pkey = object.getString("pkey");
			if (confirm.equals("yes")) {
				authenticated = true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return authenticated;
	}

	public boolean pair() {
		try {

			String confirm = null;
			String mac, pkey;
			String reply = doSend("pair");
			JSONObject object = (JSONObject) new JSONTokener(reply).nextValue();
			confirm = object.getString("confirm");
			mac = object.getString("mac");
			pkey = object.getString("pkey");
			if (confirm.equals("yes")) {
				authenticated = true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return authenticated;
	}
	
	
	public String showServerAddresses() { // Returns a string with all
											// discovered servers
		Iterator<InetAddress> iterator = iplist.iterator();
		String addresses = "";
		while (iterator.hasNext()) {
			addresses = " " + iterator.next().toString();
		}
		return addresses;
	}

	public List<InetAddress> getServerAddresses() { // returns list of
													// inetaddress objects for
													// this instance
		return iplist;
	}

	public InetAddress getBroadcast() { // returns this instances BC address
		return broadcastIP;
	}

	public String doSend(String command) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			Log.d("broadcast", "1");
			InetAddress IPAddress = InetAddress.getByName(broadcastIP.toString());
			Log.d("broadcast", "2");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = command.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			clientSocket.close();
			return modifiedSentence.trim();
		} catch (UnknownHostException e) {
			return "Unknown host: " + e.getMessage();
		} catch (IOException e) {
			return "IO Exception: " + e.getMessage();
		}
	}
	
}