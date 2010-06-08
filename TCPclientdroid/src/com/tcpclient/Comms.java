package com.tcpclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class Comms {
	public static void main(String[] args) {

	}

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

	private String getBroadcastIP() {
		String myIP = getLocalIpAddress();
		StringTokenizer tokens = new StringTokenizer(myIP, ".");
		int count = 0;
		String broadcast = "";
		while (count < 3) {
			broadcast += tokens.nextToken() + ".";
			count++;
		}
		return broadcast + "255";
	}

	public InetAddress[] findComputers() throws Exception {
		String ba = getBroadcastIP();
		InetAddress IPAddress[] = InetAddress.getAllByName(ba);
		return IPAddress;
	}

	public String doSend(String sentence) throws Exception {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress[] = InetAddress.getAllByName("192.168.0.255");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress[0], 2501);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());
			clientSocket.close();
			return modifiedSentence;
		} catch (UnknownHostException e) {
			return "Unknown host: " + e.getMessage();
		} catch (IOException e) {
			return "IO Exception: " + e.getMessage();
		}
	}

}
