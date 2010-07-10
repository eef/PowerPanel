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

import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.util.Log;

public class Comms {

	private List<InetAddress> iplist = new ArrayList<InetAddress>();
	private InetAddress broadcastIP;
	private String names = "";

	public Comms(DhcpInfo dhcp) {

		try {

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

			clientSocket.setSoTimeout(1000); // sets how long reicive() blocks
												// for once there are no new
												// packates
			long t = System.currentTimeMillis();
			long end = t + 3000;
			while (System.currentTimeMillis() < end) {
				clientSocket.receive(receivePacket);
				iplist.add(receivePacket.getAddress());
				Log.e("discovery", (receivePacket.getAddress().toString()));
				Thread.sleep(500);
				Log.e("discovery1", String.valueOf(iplist.size()));
			}
			clientSocket.close();

		} catch (UnknownHostException e) {
			Log.e("recieved:", "UnknownHostException:" + e.toString());

		} catch (IOException e) {
			Log.e("recieved:", "IOException:" + e.toString());
		}
	}

	public String showServerAddresses() { // Returns a string with all
											// discovered servers
		Log.d("showServerAddresses 1", "1");
		Iterator<InetAddress> iterator = iplist.iterator();
		Log.d("showServerAddresses 2", "2");
		while (iterator.hasNext()) {
			try {
				String name = doSend("info", iterator.next().toString().replace("/", ""));
				Log.d("showServerAddresses Iterator", name);
				names += "'" + name + "',";
			} catch (Exception e) {
				Log.d("get info", e.getMessage());
			}
		}
		Log.d("names", names.substring(0, names.length() - 1));
		return names.substring(0, names.length() - 1);
	}

	public List<InetAddress> getServerAddresses() { // returns list of
													// inetaddress objects for
													// this instance
		return iplist;
	}

	public InetAddress getBroadcast() { // returns this instances BC address
		return broadcastIP;
	}

	public String doSend(String command, String ip) throws Exception {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(ip);
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
